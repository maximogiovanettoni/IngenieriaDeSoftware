import { Gift, TrendingDown } from "lucide-react";
import { type AppliedPromotionInfo } from "@/services/OrderAPI";

interface AppliedPromotionsSectionProps {
  appliedPromotions?: AppliedPromotionInfo[];
  discountAmount: number;
}

export const AppliedPromotionsSection = ({
  appliedPromotions,
  discountAmount,
}: AppliedPromotionsSectionProps) => {
  if (!appliedPromotions || appliedPromotions.length === 0) {
    return null;
  }

  // Mapear tipos de promoción a nombres legibles
  const getPromotionTypeName = (type: string): string => {
    const typeMap: Record<string, string> = {
      BUY_X_GET_Y: "Compra X Lleva Y",
      BUY_X_PAY_Y: "Compra X Paga Y",
      PERCENTAGE_DISCOUNT: "Descuento %",
      FIXED_DISCOUNT: "Descuento Fijo",
      COMBO_DISCOUNT: "Descuento Combo",
    };
    return typeMap[type] || type;
  };

  return (
    <div
      style={{
        borderRadius: "8px",
        padding: "1.5rem",
        marginBottom: "1.5rem",
        backgroundColor: "#f59e0b20",
        border: "2px solid #f59e0b",
      }}
    >
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: "0.5rem",
          marginBottom: "1rem",
        }}
      >
        <Gift size={20} style={{ color: "#f59e0b" }} />
        <h4
          style={{
            color: "#f59e0b",
            fontSize: "0.95rem",
            fontWeight: "700",
            margin: 0,
          }}
        >
          Promociones Aplicadas
        </h4>
      </div>

      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: "0.75rem",
        }}
      >
        {appliedPromotions.map((promo, idx) => (
          <div
            key={idx}
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              padding: "0.75rem",
              backgroundColor: "var(--bg)",
              borderRadius: "6px",
              border: "1px solid var(--border)",
            }}
          >
            <div style={{ flex: 1 }}>
              <p
                style={{
                  color: "white",
                  margin: 0,
                  fontSize: "0.9rem",
                  fontWeight: "600",
                }}
              >
                {promo.name}
              </p>
              <p
                style={{
                  color: "var(--muted)",
                  margin: "0.25rem 0 0 0",
                  fontSize: "0.75rem",
                }}
              >
                {getPromotionTypeName(promo.type)}
              </p>
            </div>
            <div
              style={{
                display: "flex",
                alignItems: "center",
                gap: "0.5rem",
              }}
            >
              <TrendingDown
                size={16}
                style={{ color: "#10b981" }}
              />
              <p
                style={{
                  color: "#10b981",
                  margin: 0,
                  fontSize: "0.95rem",
                  fontWeight: "700",
                  minWidth: "70px",
                  textAlign: "right",
                }}
              >
                -${Number(promo.discountAmount).toFixed(2)}
              </p>
            </div>
          </div>
        ))}
      </div>

      {/* Total discount summary */}
      {discountAmount && discountAmount > 0 && (
        <div
          style={{
            marginTop: "1rem",
            paddingTop: "1rem",
            borderTop: "1px solid var(--border)",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <p
            style={{
              color: "var(--muted)",
              margin: 0,
              fontSize: "0.9rem",
            }}
          >
            Descuento Total
          </p>
          <p
            style={{
              color: "#10b981",
              margin: 0,
              fontSize: "1.1rem",
              fontWeight: "700",
            }}
          >
            -${Number(discountAmount).toFixed(2)}
          </p>
        </div>
      )}
    </div>
  );
};
