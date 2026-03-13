/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {setGlobalOptions} from "firebase-functions";
import {onCall, onRequest} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";
// @ts-ignore
const PayOS = require("@payos/node");


import * as admin from "firebase-admin";
import {Timestamp} from "firebase-admin/firestore";

admin.initializeApp();

// PayOS Configuration (Hardcoded for Spark plan compatibility)
const PAYOS_CLIENT_ID = "c1e69703-56bc-407b-93f0-4b92c68bd1f9";
const PAYOS_API_KEY = "ba438550-be67-4788-aed7-c1130d6f1fae";
const PAYOS_CHECKSUM_KEY =
  "dac94a49bc0a261a47469e7fccbfc6485b284326092fd8898d8199a97c0d0cd2";

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({maxInstances: 10});

type ReserveSlotRequest = {
  veterinarianId: string;
  userId?: string;
  appointmentAt: string; // ISO string
  durationMinutes: number;
  totalPrice: number;
  serviceId?: string | null;
  petId?: string | null;
  packageId?: string | null;
  notes?: string | null;
};

/**
 * Pads a number with a leading zero if needed.
 * @param {number} n The number to pad.
 * @return {string} The padded string.
 */
function pad2(n: number) {
  return n.toString().padStart(2, "0");
}

function toDateKeyUTC(date: Date) {
  return `${date.getUTCFullYear()}${pad2(date.getUTCMonth() + 1)}${pad2(date.getUTCDate())}`;
}

function toTimeKeyUTC(date: Date) {
  return `${pad2(date.getUTCHours())}${pad2(date.getUTCMinutes())}`;
}

export const reserveSlotAndCreateAppointment = onCall<ReserveSlotRequest>(
  {
    region: "asia-southeast1",
  },
  async (request) => {
    if (!request.auth?.uid) {
      throw new Error("unauthenticated");
    }

    const data = request.data;
    const userId = request.auth.uid;

    if (!data?.veterinarianId || !data?.appointmentAt) {
      throw new Error("invalid-argument");
    }

    const appointmentAt = new Date(data.appointmentAt);
    if (Number.isNaN(appointmentAt.getTime())) {
      throw new Error("invalid-argument");
    }

    const now = new Date();
    if (appointmentAt.getTime() <= now.getTime()) {
      throw new Error("failed-precondition");
    }

    const veterinarianId = data.veterinarianId;
    const durationMinutes = Number(data.durationMinutes ?? 0);
    const totalPrice = Number(data.totalPrice ?? 0);

    if (!Number.isFinite(durationMinutes) || durationMinutes <= 0) {
      throw new Error("invalid-argument");
    }
    if (!Number.isFinite(totalPrice) || totalPrice < 0) {
      throw new Error("invalid-argument");
    }

    const dateKey = toDateKeyUTC(appointmentAt);
    const timeKey = toTimeKeyUTC(appointmentAt);
    const lockId = `${veterinarianId}_${dateKey}_${timeKey}`;

    const db = admin.firestore();
    const lockRef = db.collection("doctorSlotLocks").doc(lockId);
    const appointmentsCol = db.collection("appointments");

    const result = await db.runTransaction(async (tx) => {
      const lockSnap = await tx.get(lockRef);
      if (lockSnap.exists) {
        throw new Error("already-booked");
      }

      const dupQuery = appointmentsCol
        .where("userId", "==", userId)
        .where("veterinarianId", "==", veterinarianId)
        .where("appointmentAt", "==", Timestamp.fromDate(appointmentAt))
        .where("status", "==", "PENDING_PAYMENT")
        .limit(1);

      const dupSnap = await tx.get(dupQuery);
      if (!dupSnap.empty) {
        throw new Error("duplicate-pending");
      }

      const appointmentRef = appointmentsCol.doc();

      tx.set(lockRef, {
        id: lockId,
        veterinarianId,
        appointmentAt: Timestamp.fromDate(appointmentAt),
        createdAt: Timestamp.now(),
      });

      tx.set(appointmentRef, {
        id: appointmentRef.id,
        userId,
        veterinarianId,
        serviceId: data.serviceId ?? null,
        petId: data.petId ?? null,
        packageId: data.packageId ?? null,
        status: "PENDING_PAYMENT",
        paymentStatus: "UNPAID",
        appointmentAt: Timestamp.fromDate(appointmentAt),
        durationMinutes,
        notes: data.notes ?? null,
        totalPrice,
        createdAt: Timestamp.now(),
        updatedAt: Timestamp.now(),
        slotLockId: lockId,
      });

      logger.info("Reserved slot + created appointment", {
        appointmentId: appointmentRef.id,
        lockId,
        userId,
        veterinarianId,
      });

      return {
        appointmentId: appointmentRef.id,
        lockId,
      };
    });

    return result;
  }
);

export const createPayosPaymentLink = onCall(
  {
    region: "asia-southeast1",
  },
  async (request) => {
    if (!request.auth?.uid) {
      throw new Error("unauthenticated");
    }

    const {appointmentId} = request.data as {appointmentId: string};
    if (!appointmentId) {
      throw new Error("invalid-argument: missing appointmentId");
    }

    const db = admin.firestore();
    const appointmentDoc = await db.collection("appointments")
      .doc(appointmentId).get();

    if (!appointmentDoc.exists) {
      throw new Error("not-found: appointment not found");
    }

    const appointment = appointmentDoc.data()!;
    if (appointment.userId !== request.auth.uid) {
      throw new Error("permission-denied");
    }

    const payos = new PayOS(
      PAYOS_CLIENT_ID,
      PAYOS_API_KEY,
      PAYOS_CHECKSUM_KEY
    );

    const orderCode = Date.now();
    const amount = Math.round(Number(appointment.totalPrice ?? 0));

    const domain = "vetbook-payos://payment-result"; // Deep link for app

    const body = {
      orderCode,
      amount,
      description: `VetBook appt ${appointmentId.slice(0, 5)}`,
      cancelUrl: domain,
      returnUrl: domain,
    };

    const paymentLinkRes = await payos.createPaymentLink(body);

    // Save orderCode to appointment so we can find it in webhook
    await appointmentDoc.ref.update({
      "payos.orderCode": orderCode,
      "payos.checkoutUrl": paymentLinkRes.checkoutUrl,
      "updatedAt": Timestamp.now(),
    });

    return {
      checkoutUrl: paymentLinkRes.checkoutUrl,
      orderCode: orderCode,
    };
  }
);

export const createStorePaymentLink = onCall(
  {
    region: "asia-southeast1",
  },
  async (request) => {
    if (!request.auth?.uid) {
      throw new Error("unauthenticated");
    }

    const {amount, description} =
      request.data as {amount: number; description: string};
    if (!amount) {
      throw new Error("invalid-argument: missing amount");
    }

    const payos = new PayOS(
      PAYOS_CLIENT_ID,
      PAYOS_API_KEY,
      PAYOS_CHECKSUM_KEY
    );

    const orderCode = Date.now();
    const domain = "vetbook-payos://payment-result";

    const body = {
      orderCode,
      amount: Math.round(amount),
      description: description || "VetBook Store Order",
      cancelUrl: domain,
      returnUrl: domain,
    };

    const paymentLinkRes = await payos.createPaymentLink(body);

    return {
      checkoutUrl: paymentLinkRes.checkoutUrl,
      orderCode: orderCode,
    };
  }
);

export const payosWebhook = onRequest(
  {
    region: "asia-southeast1",
  },
  async (req, res) => {
    // @ts-ignore
    const payos = new PayOS("", "", PAYOS_CHECKSUM_KEY);
    try {
      const webhookData = payos.verifyPaymentData(req.body);
      
      if (webhookData.orderCode) {
        const db = admin.firestore();
        // Check appointments
        const appointmentQuery = await db.collection("appointments")
          .where("payos.orderCode", "==", webhookData.orderCode)
          .limit(1)
          .get();

        if (!appointmentQuery.empty) {
          const appointmentDoc = appointmentQuery.docs[0];
          await appointmentDoc.ref.update({
            paymentStatus: "PAID",
            status: "CONFIRMED",
            paidAt: Timestamp.now(),
            updatedAt: Timestamp.now(),
          });
          logger.info(`Updated appointment ${appointmentDoc.id} via webhook`);
        }

        // Check store orders (if they are saved with orderCode)
        // Note: For store orders, we might need a separate collection check
        const orderQuery = await db.collection("storeOrders")
          .where("orderCode", "==", webhookData.orderCode.toString())
          .limit(1)
          .get();
        
        if (!orderQuery.empty) {
          const orderDoc = orderQuery.docs[0];
          await orderDoc.ref.update({
            status: "PAID",
            paidAt: Timestamp.now(),
          });
          logger.info(`Updated store order ${orderDoc.id} via webhook`);
        }
      }
      res.json({success: true});
    } catch (error: any) {
      logger.error("PayOS webhook error", error);
      res.status(200).json({success: false, message: error.message});
    }
  }
);
