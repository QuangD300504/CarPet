import { db } from '../firebase/config';
import { collection, getDocs, addDoc } from 'firebase/firestore';

interface SeedProduct {
    id: string;
    name: string;
    price: number;
    imageUrl?: string;
    category?: string;
}

const CUSTOMERS = [
    { uid: 'user_001', name: 'Nguyễn Văn An', phone: '0901234567', address: '123 Lê Lợi, Quận 1, HCM' },
    { uid: 'user_002', name: 'Trần Thị Bình', phone: '0912345678', address: '456 Nguyễn Huệ, Quận 1, HCM' },
    { uid: 'user_003', name: 'Lê Hoàng Nam', phone: '0923456789', address: '789 CMT8, Quận 3, HCM' },
    { uid: 'user_004', name: 'Phạm Minh Đức', phone: '0934567890', address: '101 Võ Văn Tần, Quận 3, HCM' },
    { uid: 'user_005', name: 'Đặng Thu Thảo', phone: '0945678901', address: '202 Lý Tự Trọng, Quận 1, HCM' }
];

const STATUSES = ['PAID', 'PROCESSING', 'SHIPPED', 'DELIVERED'];

export async function seedSampleOrders() {
    try {
        console.log('Starting order generation...');
        
        // 1. Get products to use in orders
        const productSnap = await getDocs(collection(db, 'products'));
        const products = productSnap.docs.map(d => ({ id: d.id, ...d.data() } as SeedProduct));
        
        if (products.length === 0) {
            console.error('No products found in Firestore. Please add products first.');
            return;
        }

        const ordersCol = collection(db, 'storeOrders');
        
        for (let i = 0; i < 20; i++) {
            const customer = CUSTOMERS[Math.floor(Math.random() * CUSTOMERS.length)];
            const orderStatus = STATUSES[Math.floor(Math.random() * STATUSES.length)];
            
            // Random items (1-4)
            const itemCount = Math.floor(Math.random() * 4) + 1;
            const selectedProducts = [...products].sort(() => 0.5 - Math.random()).slice(0, itemCount);
            
            const items = selectedProducts.map(p => {
                const qty = Math.floor(Math.random() * 3) + 1;
                return {
                    productId: p.id,
                    productName: p.name,
                    quantity: qty,
                    lineTotal: (p.price || 0) * qty,
                    imageUrl: p.imageUrl || ''
                };
            });

            const subtotal = items.reduce((sum, item) => sum + item.lineTotal, 0);
            const deliveryCharges = 20000; // Fixed delivery fee for samples
            const total = subtotal + deliveryCharges;
            
            // Random date in the last 7 days
            const date = new Date();
            date.setDate(date.getDate() - Math.floor(Math.random() * 7));
            const createdAt = date.getTime();

            const orderData = {
                uid: customer.uid,
                orderCode: `VB${Math.random().toString(36).substring(2, 8).toUpperCase()}`,
                receiverName: customer.name,
                receiverPhone: customer.phone,
                deliveryAddress: customer.address,
                items: items,
                itemCount: items.length,
                subtotal: subtotal,
                deliveryCharges: deliveryCharges,
                discount: 0,
                total: total,
                status: orderStatus,
                createdAt: createdAt,
                updatedAt: createdAt
            };

            await addDoc(ordersCol, orderData);
            console.log(`Created order ${i + 1}/20: ${orderData.orderCode}`);
        }

        console.log('Successfully generated 20 sample orders!');
        alert('Successfully generated 20 sample orders! Refresh the page to see them.');
    } catch (error) {
        console.error('Error generating orders:', error);
        alert('Error generating orders. Check console for details.');
    }
}
