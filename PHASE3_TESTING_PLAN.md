# Phase 3 Testing Plan — Reminders, Reviews & Calendar Fixes

**Date:** March 20, 2026
**Status:** Ready for Manual Testing + Automated Tests Included

---

## ✅ Automated Tests (No Manual Effort Required)

Unit tests are in `app/src/test/kotlin/com/example/vetbook/` and run with `./gradlew testDebugUnitTest`.

### `appointment/BookAppointmentViewModelTest.kt`
- ✅ `SlotOption` defaults contain exactly 9 slots (morning + afternoon)
- ✅ Slot labels parse back to correct `LocalTime`
- ✅ Available slots computed by subtracting locked slots
- ✅ Fake repository cancels appointments, returns locked slots, marks paid/completed

### `calendar/CalendarViewModelTest.kt`
- ✅ Past `UPCOMING` appointments are correctly identified for auto-complete
- ✅ `getAppointmentsForDate` filters by local date
- ✅ `hasAppointments` returns correct boolean
- ✅ Month navigation increments/decrements correctly
- ✅ Fake repository tracks completed appointment IDs

---

## ❓ Answers to Common Questions

### Q: Where do I enable notification permission?
**A:** The app requests it automatically when you try to schedule a reminder (for Android 13+). You can also pre-grant it manually:

```
Settings → Apps → VetBook → Notifications → Allow
```

On Android 12 and below, no permission is needed.

### Q: Where is WorkManager? Can I see the scheduled jobs?
**A:** WorkManager jobs are internal to the OS. You can't easily see them without developer tools. However, if a notification fires at the right time, it confirms WorkManager worked. To verify jobs exist before they fire:

```bash
# Connect device via USB, then:
adb shell dumpsys activity service androidx.work
```

Or install a WorkManager inspector app from Play Store.

### Q: Do I have to book a real appointment to test doctor ratings?
**A:** No! The **"Viết đánh giá"** button on DoctorProfileScreen now works without booking. You can open any doctor's profile and tap "Viết đánh giá" to submit a review directly.

> ⚠️ Note: The review will be submitted anonymously (userId = ""). In production, this should be tied to a completed appointment to prevent spam.

### Q: How do I test the slot lock without spending real money?
**A:** Book an appointment but **abandon** the payment (close the PayOS screen). The lock is created but no charge occurs. Then try to book the same time slot again — it should be rejected as "Time slot already booked."

### Q: Pet profile doesn't show a vaccination page?
**A:** Fixed! The vaccination section now always shows on the Pet Profile screen. Tap "Xem chi tiết →" (or "Thêm lịch tiêm chủng" if empty) to go to the full vaccination list.

---

## 📅 Calendar — Slot Grid Booking

1. Go to **VetCare** → pick a veterinarian → **Book Appointment**
2. Select a future date
3. **Before:** You should see a grid of time slots (09:00, 09:30, 10:00… 15:30) — NOT a time picker
4. ✅ Available slots: outlined chips
5. ✅ Booked/unavailable slots: greyed out, not tappable
6. Tap a slot → it becomes selected (filled chip)
7. Complete booking → payment → appointment appears in calendar

---

## 🔒 Slot Lock & TTL

1. Book an appointment at **10:00 AM**
2. Open another browser/device, try to book the **same doctor** at **10:00 AM on the same day**
3. ✅ Should be rejected ("Time slot already booked")
4. The 10:00 lock expires after 15 min if payment is abandoned

---

## ⏰ Appointment Reminders

1. Complete a booking → payment succeeds
2. **Wait 24h** (or check Settings → Apps → VetBook → Work Manager)
3. ✅ Notification fires: *"Nhắc lịch khám: Lịch khám với Dr. [Name] cho [Pet] vào HH:mm DD/MM/YYYY"*
4. Tap notification → app opens

---

## ❌ Cancel Appointment — Reminder Cancelled

1. Book and pay for an appointment
2. Open **Appointment Detail** → tap **Cancel**
3. ✅ Appointment disappears from calendar
4. ✅ Scheduled reminder is cancelled (check Work Manager — job should be gone)

---

## 🐾 Vaccination Reminders

1. Go to **Pet Profile** → **Vaccinations** → **+**
2. Fill in vaccine name, select a future date (e.g. 7 days from now)
3. Toggle **Reminder** ON
4. Save vaccination
5. ✅ A WorkManager job is scheduled for [vaccination date − 7 days]
6. Wait for the scheduled time (or check in Work Manager)
7. ✅ Notification fires: *"Nhắc tiêm phòng: [Pet] cần tiêm [Vaccine] vào ngày mai!"*

---

## ✏️ Edit Vaccination — Reminder Rescheduled

1. Add a vaccination with reminder ON
2. Open it → **Edit** → change the date
3. Save
4. ✅ Old reminder cancelled, new one scheduled for the updated date

---

## 🗑️ Delete Vaccination — Reminder Cancelled

1. Add a vaccination with reminder ON
2. Delete it
3. ✅ Reminder job is cancelled immediately

---

## ⭐ Doctor Rating — After Appointment Complete

1. Complete an appointment (it transitions to `COMPLETED`)
2. Open the appointment in **Calendar** → **Appointment Detail Sheet**
3. ✅ You see a **"Đánh giá bác sĩ"** button
4. Tap it → `RateDoctorDialog` opens
5. Select **5 stars** + write a comment → tap **Gửi đánh giá**
6. ✅ Dialog closes; success message shown

---

## 👨‍⚕️ Doctor Profile — Reviews Section

1. Go to a doctor's profile (Veterinarians → pick one)
2. Scroll down
3. ✅ You see **average star rating** (e.g. ⭐ 4.7 / 12 đánh giá)
4. ✅ Below: list of reviews with stars, comment, user name, date

---

## 📆 Past Appointments Auto-Completed

1. Have an appointment scheduled for today that already passed (e.g. was at 09:00)
2. **Open the app** (Calendar screen)
3. ✅ The appointment status should be **"Đã hoàn thành"** (COMPLETED), NOT "Sắp tới" (UPCOMING)

---

## 📱 Notification Permission

1. On Android 13+, fresh install → book a vaccination reminder
2. ✅ System prompts for notification permission
3. If denied, reminders silently skip (no crash)

---

## ⚡ Edge Cases

| # | Scenario | Expected |
|---|---|---|
| 1 | Book a slot at 23:50 | Slot grid only shows 09:00–11:30 / 14:00–15:30 |
| 2 | Submit review without selecting stars | Button disabled, cannot submit |
| 3 | Cancel already-COMPLETED appointment | No cancel button shown |
| 4 | Vaccination with reminder OFF | No WorkManager job scheduled |
| 5 | App killed before payment → lock orphaned | Cloud Function cleans up after 15 min |
| 6 | Edit reminder days (e.g. 3 days before) | Reminder rescheduled to new offset |
| 7 | Rate same appointment twice | Only one review per appointment |
| 8 | No internet when submitting review | Error message shown, retry possible |

---

## 🗂️ Where to Check WorkManager Jobs (Android)

```
Settings → Apps → VetBook → Storage & cache → "Manage Space"
OR
Settings → Apps → VetBook → "Work Manager" (if available)
OR
Shell:  adb shell am broadcast -a androidx.work.impl.tools.workmanager.DUMP_CMD
```

Jobs to look for:
- `vaccination_reminder_<id>` — vaccination reminder
- `appointment_reminder_<id>` — appointment reminder (24h before)
