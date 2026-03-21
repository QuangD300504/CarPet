import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom';
import { ProtectedLayout } from './components/Layout/ProtectedLayout';
import { AuthProvider } from './contexts/AuthContext';
import { ToastProvider } from './contexts/ToastContext';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';

import StoreAdminLayout from './pages/store/StoreAdminLayout';
import ProductsList from './pages/store/ProductsList';
import ProductForm from './pages/store/ProductForm';
import CategoriesList from './pages/store/CategoriesList';
import CategoryForm from './pages/store/CategoryForm';
import OrdersList from './pages/store/OrdersList';

import VetAdminLayout from './pages/vet/VetAdminLayout';
import VetsList from './pages/vet/VetsList';
import VetForm from './pages/vet/VetForm';
import AppointmentsList from './pages/vet/AppointmentsList';
import ClinicsList from './pages/vet/ClinicsList';
import ClinicForm from './pages/vet/ClinicForm';

import SettingsLayout from './pages/settings/SettingsLayout';
import SponsorsList from './pages/settings/SponsorsList';
import SponsorForm from './pages/settings/SponsorForm';
import ServicesList from './pages/settings/ServicesList';
import ServiceForm from './pages/settings/ServiceForm';

const router = createBrowserRouter([
  {
    path: '/',
    element: <ProtectedLayout />,
    children: [
      {
        path: '/',
        element: <Dashboard />
      },
      {
        path: 'store',
        element: <StoreAdminLayout />,
        children: [
            { index: true, element: <Navigate to="products" replace /> },
            { path: 'products', element: <ProductsList /> },
            { path: 'products/new', element: <ProductForm /> },
            { path: 'products/edit/:id', element: <ProductForm /> },
            { path: 'categories', element: <CategoriesList /> },
            { path: 'categories/new', element: <CategoryForm /> },
            { path: 'categories/edit/:id', element: <CategoryForm /> },
            { path: 'orders', element: <OrdersList /> }
        ]
      },
      {
        path: 'vets',
        element: <VetAdminLayout />,
        children: [
            { index: true, element: <Navigate to="list" replace /> },
            { path: 'list', element: <VetsList /> },
            { path: 'new', element: <VetForm /> },
            { path: 'edit/:id', element: <VetForm /> },
            { path: 'appointments', element: <AppointmentsList /> },
            { path: 'clinics', element: <ClinicsList /> },
            { path: 'clinics/new', element: <ClinicForm /> },
            { path: 'clinics/edit/:id', element: <ClinicForm /> }
        ]
      },
      {
        path: 'settings',
        element: <SettingsLayout />,
        children: [
            { index: true, element: <Navigate to="sponsors" replace /> },
            { path: 'sponsors', element: <SponsorsList /> },
            { path: 'sponsors/new', element: <SponsorForm /> },
            { path: 'sponsors/edit/:id', element: <SponsorForm /> },
            { path: 'services', element: <ServicesList /> },
            { path: 'services/new', element: <ServiceForm /> },
            { path: 'services/edit/:id', element: <ServiceForm /> }
        ]
      }
    ]
  },
  {
    path: '/login',
    element: <Login />
  }
]);

function App() {
  return (
    <AuthProvider>
      <ToastProvider>
        <RouterProvider router={router} />
      </ToastProvider>
    </AuthProvider>
  );
}

export default App;
