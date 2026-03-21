self.addEventListener('push', (event) => {
  const data = event.data?.json() ?? {};
  event.waitUntil(
    self.registration.showNotification(data.title ?? 'PetCare Reminder', {
      body: data.body,
      icon: '/icon-192.png',
      badge: '/icon-192.png',
      data: { type: data.type, refId: data.refId },
    })
  );
});

self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  event.waitUntil(
    clients.matchAll({ type: 'window' }).then((list) => {
      if (list.length > 0) return list[0].focus();
      return clients.openWindow('/');
    })
  );
});
