import { useState, useEffect } from 'react';
import { Loader2, AlertCircle, X } from 'lucide-react';
import { promotionAPI, Promotion } from '@/services/PromotionAPI';
import { useToken } from '@/services/TokenContext';
import './promotions-carousel.css';

interface PromotionCard {
  id: number;
  name: string;
  description?: string;
  type: string;
  active: boolean;
}

function PromotionDetailModal({ promotion, isOpen, onClose }: { promotion: Promotion | null; isOpen: boolean; onClose: () => void }) {
  if (!isOpen || !promotion) return null;

  const getPromotionBadge = (type: string): string => {
    const badges: Record<string, string> = {
      'PERCENTAGE_DISCOUNT': '📊',
      'FIXED_DISCOUNT': '💰',
      'BUY_X_GET_Y': '🎁',
      'BUY_X_PAY_Y': '💳',
      'FIUBA_EMAIL_DISCOUNT': '🎓',
      'PIZZA_2X1_AFTER_HOURS': '🍕',
    };
    return badges[type] || '✨';
  };

  const getPromotionDetails = (promo: Promotion): { title: string; value: string }[] => {
    const details: { title: string; value: string }[] = [];

    switch (promo.type) {
      case 'PERCENTAGE_DISCOUNT':
        if (promo.multiplier) {
          details.push({
            title: 'Descuento',
            value: `${Math.round((1 - promo.multiplier) * 100)}%`
          });
        }
        if (promo.category) {
          details.push({
            title: 'Categoría',
            value: promo.category
          });
        }
        break;
      case 'FIXED_DISCOUNT':
        if (promo.discountAmount) {
          details.push({
            title: 'Descuento',
            value: `$${Number(promo.discountAmount).toLocaleString('es-AR')}`
          });
        }
        if (promo.minimumPurchase) {
          details.push({
            title: 'Compra Mínima',
            value: `$${Number(promo.minimumPurchase).toLocaleString('es-AR')}`
          });
        }
        break;
      case 'BUY_X_GET_Y':
        if (promo.requiredProductCategory) {
          details.push({
            title: 'Compra',
            value: promo.requiredProductCategory
          });
        }
        if (promo.freeProductCategory) {
          details.push({
            title: 'Llevas Gratis',
            value: promo.freeProductCategory
          });
        }
        break;
      case 'BUY_X_PAY_Y':
        if (promo.requiredQuantity && promo.chargedQuantity) {
          details.push({
            title: 'Dinámico',
            value: `${promo.requiredQuantity}x${promo.chargedQuantity}`
          });
        }
        if (promo.category) {
          details.push({
            title: 'Categoría',
            value: promo.category
          });
        }
        break;
      case 'FIUBA_EMAIL_DISCOUNT':
        if (promo.discountPercentage) {
          details.push({
            title: 'Descuento',
            value: `${promo.discountPercentage}%`
          });
        }
        details.push({
          title: 'Válido para',
          value: 'Email @fi.uba.ar'
        });
        break;
      case 'PIZZA_2X1_AFTER_HOURS':
        if (promo.startHour) {
          details.push({
            title: 'Activada a las',
            value: `${String(promo.startHour).padStart(2, '0')}:00`
          });
        }
        details.push({
          title: 'Promoción',
          value: '2x1 en Pizzas'
        });
        break;
    }

    if (promo.applicableDays && promo.applicableDays.length > 0) {
      details.push({
        title: 'Válida en',
        value: promo.applicableDays.join(', ')
      });
    }

    return details;
  };

  const details = getPromotionDetails(promotion);

  return (
    <div className="modal modal--promo" onClick={onClose}>
      <div className="modal__backdrop" />
      <div className="modal__card modal__card--promo" onClick={(e) => e.stopPropagation()}>
        <button className="modal__close" onClick={onClose}>
          <X size={20} />
        </button>

        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '12px', 
          marginBottom: '16px' 
        }}>
          <div style={{ fontSize: '40px' }}>{getPromotionBadge(promotion.type)}</div>
          <div>
            <div style={{ fontSize: '12px', color: 'var(--muted)' }}>
              {promotion.type ? promotion.type.replace(/_/g, ' ') : 'Promoción'}
            </div>
            <h2 className="modal__title" style={{ margin: '4px 0 0 0' }}>
              {promotion.name}
            </h2>
          </div>
        </div>

        <p className="modal__description" style={{ marginBottom: '24px' }}>
          {promotion.description || 'Promoción especial disponible.'}
        </p>

        {/* Detalles principales */}
        {details.length > 0 && (
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(2, 1fr)',
            gap: '12px',
            padding: '16px',
            background: 'var(--panel-2)',
            borderRadius: '12px',
            marginBottom: '16px'
          }}>
            {details.map((detail, idx) => (
              <div key={idx} style={{ textAlign: 'center' }}>
                <div style={{ 
                  fontSize: '11px', 
                  color: 'var(--muted)', 
                  marginBottom: '4px',
                  textTransform: 'uppercase',
                  fontWeight: '600'
                }}>
                  {detail.title}
                </div>
                <div style={{ 
                  fontSize: '16px', 
                  fontWeight: '700',
                  color: 'var(--blue-2)'
                }}>
                  {detail.value}
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Fechas */}
        {(promotion.startDate || promotion.endDate) && (
          <div style={{
            padding: '12px',
            background: 'var(--panel-2)',
            borderRadius: '8px',
            marginBottom: '16px',
            fontSize: '13px',
            color: 'var(--text)'
          }}>
            <div style={{ marginBottom: '4px' }}>
              📅 <strong>Vigencia:</strong>
            </div>
            <div style={{ color: 'var(--muted)', marginLeft: '20px' }}>
              {promotion.startDate && `Desde ${promotion.startDate}`}
              {promotion.startDate && promotion.endDate && ' • '}
              {promotion.endDate && `Hasta ${promotion.endDate}`}
            </div>
          </div>
        )}

        <div className="modal__actions">
          <button className="btn btn--primary" onClick={onClose}>
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
}

export const PromotionsCarousel = () => {
  const [tokenState] = useToken();
  const [promotions, setPromotions] = useState<PromotionCard[]>([]);
  const [fullPromotions, setFullPromotions] = useState<Promotion[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedPromotion, setSelectedPromotion] = useState<Promotion | null>(null);

  // Check if a promotion is valid today (date range + applicable days + time-based rules)
  const isPromotionValid = (promo: Promotion): boolean => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    // Check if within date range
    if (promo.startDate) {
      const startDate = new Date(promo.startDate);
      startDate.setHours(0, 0, 0, 0);
      if (today < startDate) {
        return false;
      }
    }

    if (promo.endDate) {
      const endDate = new Date(promo.endDate);
      endDate.setHours(0, 0, 0, 0);
      if (today > endDate) {
        return false;
      }
    }

    // Check applicable days
    if (promo.applicableDays && promo.applicableDays.length > 0) {
      const dayNames = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];
      const currentDay = dayNames[today.getDay()];
      if (!promo.applicableDays.includes(currentDay)) {
        return false;
      }
    }

    // Check time-based restrictions for PIZZA_2X1_AFTER_HOURS
    if (promo.type === 'PIZZA_2X1_AFTER_HOURS') {
      const now = new Date();
      const currentHour = now.getHours();
      if (promo.startHour !== undefined && currentHour < promo.startHour) {
        return false;
      }
    }

    return true;
  };

  useEffect(() => {
    const loadPromotions = async () => {
      setLoading(true);
      setError(null);
      try {
        const token = tokenState.state === 'LOGGED_IN' ? tokenState.tokens.accessToken : undefined;
        const data = await promotionAPI.getAllPromotions(token);
        console.log('✨ Promociones cargadas:', data);
        
        // Filtrar solo promociones activas Y válidas hoy
        const activeValidPromotions = data.filter(p => p.active && isPromotionValid(p));
        console.log('✨ Promociones activas y válidas filtradas:', activeValidPromotions);
        setFullPromotions(activeValidPromotions);
        setPromotions(activeValidPromotions.map(p => ({
          id: p.id,
          name: p.name,
          description: p.description,
          type: p.type,
          active: p.active,
        })));
      } catch (err) {
        const message = err instanceof Error ? err.message : 'Error cargando promociones';
        setError(message);
        console.error('❌ Error loading promotions:', err);
      } finally {
        setLoading(false);
      }
    };

    if (tokenState.state === 'LOGGED_IN') {
      loadPromotions();
    }
  }, [tokenState]);

  if (error) {
    return (
      <div className="promo-carousel promo-carousel--error">
        <AlertCircle size={18} />
        <span>Error cargando promociones</span>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="promo-carousel promo-carousel--loading">
        <Loader2 size={18} className="spin" />
        <span>Cargando promociones...</span>
      </div>
    );
  }

  if (promotions.length === 0) {
    return null; // No mostrar nada si no hay promociones
  }

  return (
    <section className="promo-carousel">
      <div className="promo-carousel__header">
        <h2 className="promo-carousel__title">✨ Promociones Activas</h2>
        <span className="promo-carousel__count">{promotions.length} oferta{promotions.length !== 1 ? 's' : ''}</span>
      </div>

      <div className="promo-carousel__scroll">
        <div className="promo-carousel__items">
          {promotions.map((promo, index) => {
            const fullPromo = fullPromotions[index];
            
            return (
              <button 
                key={promo.id} 
                className="promo-card" 
                onClick={() => setSelectedPromotion(fullPromo)}
              >
                <div className="promo-card__badge">
                  {promo.type === 'PERCENTAGE_DISCOUNT' && '📊'}
                  {promo.type === 'FIXED_DISCOUNT' && '💰'}
                  {promo.type === 'BUY_X_GET_Y' && '🎁'}
                  {promo.type === 'BUY_X_PAY_Y' && '💳'}
                </div>
                
                <h3 className="promo-card__title">{promo.name}</h3>
                
                <p className="promo-card__desc">{promo.description || 'Promoción especial'}</p>
                
                {/* Mostrar info relevante según tipo */}
                <div style={{
                  display: 'flex',
                  gap: '8px',
                  marginTop: '8px',
                  marginBottom: '8px',
                  flexWrap: 'wrap'
                }}>
                  {fullPromo?.multiplier && promo.type === 'PERCENTAGE_DISCOUNT' && (
                    <span style={{
                      fontSize: '12px',
                      padding: '4px 8px',
                      background: 'var(--panel-2)',
                      borderRadius: '4px',
                      color: 'var(--blue-2)',
                      fontWeight: '600'
                    }}>
                      -{Math.round((1 - fullPromo.multiplier) * 100)}%
                    </span>
                  )}
                  {fullPromo?.discountAmount && promo.type === 'FIXED_DISCOUNT' && (
                    <span style={{
                      fontSize: '12px',
                      padding: '4px 8px',
                      background: 'var(--panel-2)',
                      borderRadius: '4px',
                      color: 'var(--blue-2)',
                      fontWeight: '600'
                    }}>
                      ${Number(fullPromo.discountAmount).toLocaleString('es-AR')}
                    </span>
                  )}
                </div>
                
                <div className="promo-card__indicator">
                  <span className="dot"></span>
                  <span className="text">Ver detalles</span>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      <PromotionDetailModal promotion={selectedPromotion} isOpen={!!selectedPromotion} onClose={() => setSelectedPromotion(null)} />
    </section>
  );
};
