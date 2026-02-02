import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import AppLayout from "../layout/AppLayout";

const LoginPage = lazy(() => import("../../features/auth/pages/LoginPage"));
const ArtistasPage = lazy(() => import("../../features/artistas/pages/ArtistasPage"));
const ArtistaDetalhePage = lazy(() => import("../../features/artistas/pages/ArtistaDetalhePage"))

function Loading() {
  return (
    <div className="flex justify-content-center align-items-center" style={{ minHeight: 240 }}>
      <i className="pi pi-spin pi-spinner" style={{ fontSize: 22 }} />
    </div>
  );
}

export default function AppRoutes() {
  return (
    <Suspense fallback={<Loading />}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route index element={<Navigate to="/artistas" replace />} />

            <Route path="/artistas" element={<ArtistasPage />} />
            <Route path="/artistas/:id" element={<ArtistaDetalhePage />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/artistas" replace />} />
      </Routes>
    </Suspense>
  );
}