import { CommonLayout } from "@/components/CommonLayout/CommonLayout";
import { Link } from "wouter";

export const MainScreen = () => {
  return (
    <CommonLayout>
      <div className="max-w-3xl mx-auto py-12 px-4">
        <h1 className="text-3xl font-bold mb-6">Bienvenido</h1>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <Link href="/menu" className="block p-6 bg-white rounded-lg shadow hover:shadow-lg text-center">
            <h2 className="text-xl font-semibold">Menú</h2>
            <p className="text-sm text-gray-500 mt-2">Ver el menú y agregar items al carrito</p>
          </Link>

          <Link href="/dashboard" className="block p-6 bg-gradient-to-br from-purple-50 to-blue-50 rounded-lg shadow hover:shadow-lg text-center border-2 border-purple-200">
            <h2 className="text-xl font-semibold">📊 Dashboard</h2>
            <p className="text-sm text-gray-600 mt-2">Ver estadísticas, tracking en vivo y calificaciones</p>
          </Link>

          <Link href="/orders" className="block p-6 bg-white rounded-lg shadow hover:shadow-lg text-center">
            <h2 className="text-xl font-semibold">Mis Pedidos</h2>
            <p className="text-sm text-gray-500 mt-2">Ver tus pedidos y puntos de retiro</p>
          </Link>

          <Link href="/profile" className="block p-6 bg-white rounded-lg shadow hover:shadow-lg text-center">
            <h2 className="text-xl font-semibold">Perfil</h2>
            <p className="text-sm text-gray-500 mt-2">Ver y editar tu perfil</p>
          </Link>

          <Link href="/menu?openCart=1" className="block p-6 bg-white rounded-lg shadow hover:shadow-lg text-center">
            <h2 className="text-xl font-semibold">Carrito</h2>
            <p className="text-sm text-gray-500 mt-2">Ver tu carrito</p>
          </Link>
        </div>
      </div>
    </CommonLayout>
  );
};
