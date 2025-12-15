import { AlertCircle, X } from "lucide-react";

export interface OrderErrorDetails {
  productName?: string;
  requested?: number;
  available?: number;
}

interface OrderErrorModalProps {
  isOpen: boolean;
  onClose: () => void;
  onClearCart: () => void;
  errorMessage: string;
  errorDetails?: OrderErrorDetails;
}

export const OrderErrorModal = ({ isOpen, onClose, onClearCart, errorMessage, errorDetails }: OrderErrorModalProps) => {
  if (!isOpen) return null;

  const isSufficientStockError = errorMessage.includes("stock");

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div className="fixed inset-0 bg-black/50" onClick={onClose} />
      <div className="relative bg-[#2a2a2a] border border-[#4a5565] rounded-lg p-6 max-w-md w-full">
        {/* Header */}
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="bg-[rgba(239,68,68,0.2)] p-2 rounded-full">
              <AlertCircle size={20} className="text-[#EF4444]" />
            </div>
            <h2 className="text-white text-[18px] font-bold">Error en el Pedido</h2>
          </div>
          <button onClick={onClose} className="text-[#99a1af] hover:text-white">
            <X size={20} />
          </button>
        </div>

        {/* Error Message */}
        <div className="bg-[rgba(239,68,68,0.1)] border border-[#EF4444]/50 rounded-lg p-4 mb-4">
          <p className="text-white text-[13px]">{errorMessage}</p>
        </div>

        {/* Error Details */}
        {isSufficientStockError && errorDetails && (
          <div className="bg-[rgba(240,177,0,0.1)] border border-[#F0B100]/50 rounded-lg p-3 mb-4">
            <p className="text-[#F0B100] text-[12px] font-semibold mb-2">{errorDetails.productName}</p>
            <div className="space-y-1 text-[11px] text-[#99a1af]">
              <p>Solicitado: {errorDetails.requested} unidades</p>
              <p>Disponible: {errorDetails.available} unidades</p>
            </div>
            <div className="mt-3 p-2 bg-[rgba(255,255,255,0.05)] rounded text-[11px] text-[#99a1af] space-y-1">
              <p className="font-semibold text-white">💡 Sugerencias:</p>
              <ul className="list-disc list-inside">
                <li>Reduce la cantidad de este producto</li>
                <li>Elimina este producto del carrito</li>
              </ul>
            </div>
          </div>
        )}

        {/* Actions */}
        <div className="flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 bg-transparent border border-[#4a5565] text-white py-2 rounded-lg hover:bg-[rgba(38,38,38,0.3)] transition-colors text-[13px] font-medium"
          >
            Entendido
          </button>
          <button
            onClick={() => {
              onClearCart();
              onClose();
            }}
            className="flex-1 bg-gradient-to-r from-[#EF4444] to-[#DC2626] text-white py-2 rounded-lg hover:opacity-90 transition-opacity text-[13px] font-medium"
          >
            Vaciar Carrito
          </button>
        </div>
      </div>
    </div>
  );
};
