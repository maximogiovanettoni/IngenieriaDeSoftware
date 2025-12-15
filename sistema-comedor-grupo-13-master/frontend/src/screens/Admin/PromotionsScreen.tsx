import { useState, useEffect } from 'react';
import { Plus, Trash2, Search, AlertCircle, Loader2, ArrowLeft, Edit2, XCircle, Percent, Check } from 'lucide-react';
import { promotionAPI, Promotion } from '@/services/PromotionAPI';
import { useToken } from '@/services/TokenContext';
import { useLocation } from 'wouter';
import './promotions-screen.css';

type PromotionType = 'PERCENTAGE_DISCOUNT' | 'FIXED_DISCOUNT' | 'BUY_X_GET_Y' | 'BUY_X_PAY_Y';
type ProductCategory = 'SANDWICH' | 'DRINK' | 'DESSERT' | 'SALAD' | 'MAIN_COURSE' | 'COMBO' | 'SIDE_DISH' | 'COFFEE';
type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

interface PromotionFormData {
  name: string;
  description: string;
  active: boolean;
  startDate: string;
  endDate: string;
  applicableDays: Set<DayOfWeek>;
  type: PromotionType;
  category: ProductCategory;
  categoryFree?: ProductCategory;
  discount?: number;
  fixedAmount?: number;
  quantityToBuy?: number;
  quantityToGet?: number;
  payAmount?: number;
}

export default function PromotionsScreen() {
  const [tokenState] = useToken();
  const [, setLocation] = useLocation();

  const [searchTerm, setSearchTerm] = useState('');
  const [promotions, setPromotions] = useState<Promotion[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [addDialogOpen, setAddDialogOpen] = useState(false);
  const [editDialog, setEditDialog] = useState<{ open: boolean; promotion: Promotion | null }>({ open: false, promotion: null });
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; promotion: Promotion | null }>({ open: false, promotion: null });
  const [toggleDialog, setToggleDialog] = useState<{ open: boolean; promotion: Promotion | null }>({ open: false, promotion: null });
  const [formLoading, setFormLoading] = useState(false);
  
  const [formData, setFormData] = useState<PromotionFormData>({
    name: '',
    description: '',
    active: true,
    startDate: '',
    endDate: '',
    applicableDays: new Set(),
    type: 'PERCENTAGE_DISCOUNT',
    category: 'SANDWICH',
  });

  useEffect(() => {
    if (tokenState.state !== 'LOGGED_IN') setLocation('/login');
  }, [tokenState.state, setLocation]);

  useEffect(() => {
    if (tokenState.state === 'LOGGED_IN') {
      (async () => {
        setLoading(true); 
        setError(null);
        try {
          const token = tokenState.tokens.accessToken;
          const data = await promotionAPI.getAllPromotions(token);
          setPromotions(data);
        } catch (err) { 
          setError(err instanceof Error ? err.message : 'Error cargando promociones'); 
        }
        finally { 
          setLoading(false); 
        }
      })();
    }
  }, [tokenState]);

  useEffect(() => {
    if (editDialog.open && editDialog.promotion) {
      const promo = editDialog.promotion;
      const promoData = promo as unknown as Record<string, unknown>;
      setFormData({
        name: promo.name,
        description: promo.description || '',
        active: promo.active,
        startDate: promo.startDate || '',
        endDate: promo.endDate || '',
        applicableDays: new Set((promo.applicableDays || []) as DayOfWeek[]),
        type: promo.type as PromotionType,
        category: (promoData.category as ProductCategory) || (promoData.requiredProductCategory as ProductCategory) || 'SANDWICH',
        categoryFree: (promoData.freeProductCategory as ProductCategory) || undefined,
        discount: promoData.discount as number,
        fixedAmount: (promoData.discountAmount as number) || (promoData.minimumPurchase as number),
        quantityToBuy: (promoData.requiredQuantity as number),
        quantityToGet: (promoData.freeQuantity as number),
        payAmount: promoData.chargedQuantity as number,
      });
    }
  }, [editDialog.open, editDialog.promotion]);

  const reloadPromotions = async () => {
    if (tokenState.state === 'LOGGED_IN') {
      setLoading(true);
      try {
        const token = tokenState.tokens.accessToken;
        const data = await promotionAPI.getAllPromotions(token);
        setPromotions(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Error cargando promociones');
      } finally {
        setLoading(false);
      }
    }
  };

  const openCreate = () => { 
    setFormData({ name: '', description: '', active: true, startDate: '', endDate: '', applicableDays: new Set(), type: 'PERCENTAGE_DISCOUNT', category: 'SANDWICH' });
    setAddDialogOpen(true); 
  };
  const openEdit = (promotion: Promotion) => { setEditDialog({ open: true, promotion }); };
  const openDelete = (promotion: Promotion) => { setDeleteDialog({ open: true, promotion }); };
  const openToggle = (promotion: Promotion) => { setToggleDialog({ open: true, promotion }); };

  const handleFormChange = (field: keyof PromotionFormData, value: string | number | boolean | Set<DayOfWeek> | ProductCategory) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const toggleDay = (day: DayOfWeek) => {
    setFormData(prev => {
      const newDays = new Set(prev.applicableDays);
      if (newDays.has(day)) newDays.delete(day);
      else newDays.add(day);
      return { ...prev, applicableDays: newDays };
    });
  };

  const handleSavePromotion = async () => {
    if (!formData.name.trim()) { setError('El nombre es obligatorio'); return; }
    if (!formData.type) { setError('El tipo de promoción es obligatorio'); return; }
    
    // Validar categoría solo si es necesaria para este tipo
    if (['PERCENTAGE_DISCOUNT', 'BUY_X_PAY_Y', 'BUY_X_GET_Y'].includes(formData.type)) {
      if (!formData.category) { setError('La categoría es obligatoria'); return; }
    }
    
    // Para BUY_X_GET_Y, validar que categoryFree sea diferente y esté especificado
    if (formData.type === 'BUY_X_GET_Y') {
      if (!formData.categoryFree) { setError('La categoría del producto gratis es obligatoria'); return; }
      if (formData.categoryFree === formData.category) { setError('Las categorías de compra y regalo deben ser diferentes'); return; }
    }

    setFormLoading(true);
    try {
      const token = tokenState.state === 'LOGGED_IN' ? tokenState.tokens?.accessToken : undefined;
      
      // Si es actualización, solo enviar campos comunes
      if (editDialog.open && editDialog.promotion) {
        const updatePayload = {
          name: formData.name,
          description: formData.description || null,
          active: formData.active,
          startDate: formData.startDate || null,
          endDate: formData.endDate || null,
          applicableDays: Array.from(formData.applicableDays),
        };
        await promotionAPI.updatePromotion(editDialog.promotion.id, updatePayload, token);
      } else {
        // Si es creación, enviar todo incluyendo campos específicos
        const payload: Record<string, unknown> = {
          type: formData.type,
          name: formData.name,
          description: formData.description || null,
          active: formData.active,
          startDate: formData.startDate || null,
          endDate: formData.endDate || null,
          applicableDays: Array.from(formData.applicableDays),
        };

        // Agregar campos específicos según el tipo de promoción
        if (formData.type === 'PERCENTAGE_DISCOUNT') {
          payload.category = formData.category;
          payload.discount = formData.discount ? parseInt(formData.discount.toString()) : 0;
        } else if (formData.type === 'FIXED_DISCOUNT') {
          payload.minimumPurchase = formData.quantityToBuy ? parseFloat(formData.quantityToBuy.toString()) : 0;
          payload.discountAmount = formData.fixedAmount ? parseFloat(formData.fixedAmount.toString()) : 0;
        } else if (formData.type === 'BUY_X_GET_Y') {
          const reqQty = formData.quantityToBuy ? parseInt(formData.quantityToBuy.toString()) : 1;
          const freeQty = formData.quantityToGet ? parseInt(formData.quantityToGet.toString()) : 1;
          if (isNaN(reqQty) || reqQty < 1 || isNaN(freeQty) || freeQty < 1) {
            setError('Las cantidades deben ser números positivos');
            setFormLoading(false);
            return;
          }
          payload.requiredProductCategory = formData.category;
          payload.freeProductCategory = formData.categoryFree;
          payload.requiredQuantity = reqQty;
          payload.freeQuantity = freeQty;
        } else if (formData.type === 'BUY_X_PAY_Y') {
          const reqQty = formData.quantityToBuy ? parseInt(formData.quantityToBuy.toString()) : 1;
          const chargeQty = formData.quantityToGet ? parseInt(formData.quantityToGet.toString()) : 1;
          if (isNaN(reqQty) || reqQty < 1 || isNaN(chargeQty) || chargeQty < 1) {
            setError('Las cantidades deben ser números positivos');
            setFormLoading(false);
            return;
          }
          if (chargeQty >= reqQty) {
            setError('La cantidad a pagar debe ser menor que la cantidad requerida');
            setFormLoading(false);
            return;
          }
          payload.category = formData.category;
          payload.requiredQuantity = reqQty;
          payload.chargedQuantity = chargeQty;
        }
        
        await promotionAPI.createPromotion(payload, token);
      }

      await reloadPromotions();
      setAddDialogOpen(false);
      setEditDialog({ open: false, promotion: null });
      setError(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error guardando promoción');
    } finally {
      setFormLoading(false);
    }
  };

  const confirmDelete = async () => {
    if (!deleteDialog.promotion) return;
    setFormLoading(true);
    try {
      const token = tokenState.state === 'LOGGED_IN' ? tokenState.tokens.accessToken : undefined;
      await promotionAPI.deletePromotion(deleteDialog.promotion.id, token);
      
      setDeleteDialog({ open: false, promotion: null });
      await reloadPromotions();
    } catch (err) { setError(err instanceof Error ? err.message : 'Error eliminando promoción'); }
    finally { setFormLoading(false); }
  };

  const confirmToggle = async () => {
    if (!toggleDialog.promotion) return;
    setFormLoading(true);
    try {
      const token = tokenState.state === 'LOGGED_IN' ? tokenState.tokens.accessToken : undefined;
      if (toggleDialog.promotion.active) await promotionAPI.deactivatePromotion(toggleDialog.promotion.id, token);
      else await promotionAPI.activatePromotion(toggleDialog.promotion.id, token);
      
      setToggleDialog({ open: false, promotion: null });
      await reloadPromotions();
    } catch (err) { setError(err instanceof Error ? err.message : 'Error actualizando promoción'); }
    finally { setFormLoading(false); }
  };

  const filtered = promotions.filter(p => p.name.toLowerCase().includes(searchTerm.toLowerCase()) || (p.description || '').toLowerCase().includes(searchTerm.toLowerCase()));

  return (
    <div className="page">
      <div className="container">
        <div className="header">
          <div className="badge"><Percent size={28} className="badge__ico" /></div>
          <p className="header__title">Gestión de Promociones</p>
        </div>

        {error && !addDialogOpen && !editDialog.open && (
          <div className="alert alert--error">
            <AlertCircle size={18} />
            <span>{error}</span>
            <button className="alert__close" onClick={() => setError(null)}>
              <XCircle size={18} />
            </button>
          </div>
        )}

        <section className="card card--glow">
          <div className="card__inner">
            <div className="card__head">
              <p className="card__title">Promociones</p>
              <button className="btn btn--grad" onClick={openCreate} disabled={loading}>
                <Plus size={16} /> <span>Nueva Promoción</span>
              </button>
            </div>

            <div className="filters">
              <div className="input input--with-ico">
                <Search size={16} className="input__ico" />
                <input type="text" placeholder="Buscar promociones…" value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
              </div>
            </div>

            <div className="table">
              <div className="table__scroll">
                <table>
                  <thead>
                    <tr>
                      <th>Nombre</th>
                      <th className="hide-md">Descripción</th>
                      <th className="hide-md">Tipo</th>
                      <th className="hide-md">Desde</th>
                      <th className="hide-md">Hasta</th>
                      <th className="hide-md text-center">Estado</th>
                      <th className="text-right">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {loading ? (
                      <tr><td colSpan={7} className="table__center muted"><Loader2 size={16} className="spin" /> Cargando promociones…</td></tr>
                    ) : filtered.length === 0 ? (
                      <tr><td colSpan={7} className="table__center muted">No hay promociones</td></tr>
                    ) : (
                      filtered.map(promotion => (
                        <tr key={promotion.id}>
                          <td>{promotion.name}</td>
                          <td className="muted hide-md">{promotion.description || '-'}</td>
                          <td className="muted hide-md">{promotion.type ? promotion.type.replace(/_/g, ' ') : '-'}</td>
                          <td className="muted hide-md">{promotion.startDate || '-'}</td>
                          <td className="muted hide-md">{promotion.endDate || '-'}</td>
                          <td className="muted hide-md text-center">{promotion.active ? <span className="pill pill--ok">Activo</span> : <span className="pill pill--bad">Inactivo</span>}</td>
                          <td className="text-right">
                            <div className="row-end">
                              <button className="icon-btn info" onClick={() => openEdit(promotion)} title="Editar">
                                <Edit2 size={14} />
                              </button>
                              <button className="icon-btn warn" onClick={() => openToggle(promotion)} title="Activar/Desactivar">
                                {promotion.active ? <XCircle size={14} /> : <Check size={14} />}
                              </button>
                              <button className="icon-btn danger" onClick={() => openDelete(promotion)} title="Eliminar">
                                <Trash2 size={14} />
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            <button className="back" onClick={() => setLocation('/admin')}>
              <ArrowLeft size={14} /> Volver a Menú Inicial
            </button>
          </div>
        </section>

        {/* Edit modal - Solo campos editables */}
        <div className={`modal ${editDialog.open ? 'is-open' : ''}`} style={{ display: editDialog.open ? 'block' : 'none' }}>
          <div className="modal__backdrop" onClick={() => setEditDialog({ open: false, promotion: null })} />
          <div className="modal__card" style={{ maxHeight: '90vh', overflowY: 'auto' }}>
            <h2 className="modal__title">Editar Promoción</h2>
            <p className="modal__sub">Editando: <strong>{editDialog.promotion?.name}</strong></p>
            
            {error && editDialog.open && (
              <div className="alert alert--error" style={{ marginBottom: '16px' }}>
                <AlertCircle size={18} />
                <span>{error}</span>
                <button className="alert__close" onClick={() => setError(null)}>
                  <XCircle size={18} />
                </button>
              </div>
            )}
            
            <form className="modal__content" onSubmit={(e) => { e.preventDefault(); handleSavePromotion(); }}>
              
              {/* Nombre y Descripción */}
              <label className="field"><span>Nombre *</span><input value={formData.name} onChange={(e) => handleFormChange('name', e.target.value)} placeholder="Nombre de la promoción" /></label>
              <label className="field"><span>Descripción</span><textarea value={formData.description} onChange={(e) => handleFormChange('description', e.target.value)} placeholder="Descripción (opcional)" rows={2} /></label>

              {/* Fechas */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <label className="field"><span>Fecha Inicio</span><input type="date" value={formData.startDate} onChange={(e) => handleFormChange('startDate', e.target.value)} /></label>
                <label className="field"><span>Fecha Fin</span><input type="date" value={formData.endDate} onChange={(e) => handleFormChange('endDate', e.target.value)} /></label>
              </div>

              {/* Días Aplicables */}
              <label className="field"><span>Días Aplicables</span>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(120px, 1fr))', gap: '0.5rem', marginTop: '0.5rem' }}>
                  {['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'].map(day => (
                    <label key={day} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                      <input type="checkbox" checked={formData.applicableDays.has(day as DayOfWeek)} onChange={() => toggleDay(day as DayOfWeek)} />
                      <span>{['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'][['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'].indexOf(day)]}</span>
                    </label>
                  ))}
                </div>
              </label>

              {/* Estado */}
              <label className="field"><span>Estado</span>
                <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                    <input type="radio" checked={formData.active} onChange={() => handleFormChange('active', true)} />
                    <span>Activo</span>
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                    <input type="radio" checked={!formData.active} onChange={() => handleFormChange('active', false)} />
                    <span>Inactivo</span>
                  </label>
                </div>
              </label>

              <div className="modal__actions">
                <button type="button" className="btn btn--ghost" onClick={() => setEditDialog({ open: false, promotion: null })}>Cancelar</button>
                <button type="submit" className="btn btn--grad" disabled={formLoading}>{formLoading ? <Loader2 size={16} className="spin" /> : null} Guardar</button>
              </div>
            </form>
          </div>
        </div>

        {/* Create modal - Todos los campos */}
        <div className={`modal ${addDialogOpen ? 'is-open' : ''}`} style={{ display: addDialogOpen ? 'block' : 'none' }}>
          <div className="modal__backdrop" onClick={() => setAddDialogOpen(false)} />
          <div className="modal__card" style={{ maxHeight: '90vh', overflowY: 'auto' }}>
            <h2 className="modal__title">Nueva Promoción</h2>
            <p className="modal__sub">Complete todos los campos requeridos</p>
            
            {error && addDialogOpen && (
              <div className="alert alert--error" style={{ marginBottom: '16px' }}>
                <AlertCircle size={18} />
                <span>{error}</span>
                <button className="alert__close" onClick={() => setError(null)}>
                  <XCircle size={18} />
                </button>
              </div>
            )}
            
            <form className="modal__content" onSubmit={(e) => { e.preventDefault(); handleSavePromotion(); }}>
              
              {/* Nombre y Descripción */}
              <label className="field"><span>Nombre *</span><input value={formData.name} onChange={(e) => handleFormChange('name', e.target.value)} placeholder="Nombre de la promoción" /></label>
              <label className="field"><span>Descripción</span><textarea value={formData.description} onChange={(e) => handleFormChange('description', e.target.value)} placeholder="Descripción (opcional)" rows={2} /></label>

              {/* Tipo de Promoción y Categoría */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <label className="field"><span>Tipo de Promoción *</span>
                  <select value={formData.type} onChange={(e) => handleFormChange('type', e.target.value as PromotionType)}>
                    <option value="PERCENTAGE_DISCOUNT">Descuento Porcentual (%)</option>
                    <option value="FIXED_DISCOUNT">Descuento Fijo ($)</option>
                    <option value="BUY_X_GET_Y">Compra X Lleva Y</option>
                    <option value="BUY_X_PAY_Y">Compra X Paga Y</option>
                  </select>
                </label>
                {formData.type !== 'FIXED_DISCOUNT' && formData.type !== 'BUY_X_GET_Y' && (
                  <label className="field"><span>Categoría *</span>
                    <select value={formData.category} onChange={(e) => handleFormChange('category', e.target.value as ProductCategory)}>
                      <option value="SANDWICH">Sándwich</option>
                      <option value="DRINK">Bebida</option>
                      <option value="DESSERT">Postre</option>
                      <option value="SALAD">Ensalada</option>
                      <option value="MAIN_COURSE">Plato Principal</option>
                      <option value="COMBO">Combo</option>
                      <option value="SIDE_DISH">Acompañamiento</option>
                      <option value="COFFEE">Café</option>
                    </select>
                  </label>
                )}
              </div>

              {/* Categorías para BUY_X_GET_Y */}
              {formData.type === 'BUY_X_GET_Y' && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <label className="field"><span>Categoría a Comprar *</span>
                    <select value={formData.category} onChange={(e) => handleFormChange('category', e.target.value as ProductCategory)}>
                      <option value="SANDWICH">Sándwich</option>
                      <option value="DRINK">Bebida</option>
                      <option value="DESSERT">Postre</option>
                      <option value="SALAD">Ensalada</option>
                      <option value="MAIN_COURSE">Plato Principal</option>
                      <option value="COMBO">Combo</option>
                      <option value="SIDE_DISH">Acompañamiento</option>
                      <option value="COFFEE">Café</option>
                    </select>
                  </label>
                  <label className="field"><span>Categoría a Llevar Gratis *</span>
                    <select value={formData.categoryFree || 'DRINK'} onChange={(e) => handleFormChange('categoryFree', e.target.value as ProductCategory)}>
                      <option value="SANDWICH">Sándwich</option>
                      <option value="DRINK">Bebida</option>
                      <option value="DESSERT">Postre</option>
                      <option value="SALAD">Ensalada</option>
                      <option value="MAIN_COURSE">Plato Principal</option>
                      <option value="COMBO">Combo</option>
                      <option value="SIDE_DISH">Acompañamiento</option>
                      <option value="COFFEE">Café</option>
                    </select>
                  </label>
                </div>
              )}

              {/* Campos específicos según tipo */}
              {formData.type === 'PERCENTAGE_DISCOUNT' && (
                <label className="field"><span>Descuento (%) *</span><input type="number" min="0" max="100" value={formData.discount || 0} onChange={(e) => handleFormChange('discount', parseFloat(e.target.value))} placeholder="Ej: 10" /></label>
              )}
              {formData.type === 'FIXED_DISCOUNT' && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <label className="field"><span>Compra Mínima ($)</span><input type="number" min="0" step="0.01" value={formData.quantityToBuy || 0} onChange={(e) => handleFormChange('quantityToBuy', parseFloat(e.target.value))} placeholder="Ej: 10.00" /></label>
                  <label className="field"><span>Descuento ($) *</span><input type="number" min="0.01" step="0.01" value={formData.fixedAmount || 0} onChange={(e) => handleFormChange('fixedAmount', parseFloat(e.target.value))} placeholder="Ej: 5.00" /></label>
                </div>
              )}
              {formData.type === 'BUY_X_GET_Y' && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <label className="field"><span>Cantidad a Comprar *</span><input type="text" value={formData.quantityToBuy || ''} onChange={(e) => { const val = e.target.value; handleFormChange('quantityToBuy', val === '' ? '' : Math.max(1, parseInt(val) || 1)); }} placeholder="1" /></label>
                  <label className="field"><span>Cantidad a Llevar *</span><input type="text" value={formData.quantityToGet || ''} onChange={(e) => { const val = e.target.value; handleFormChange('quantityToGet', val === '' ? '' : Math.max(1, parseInt(val) || 1)); }} placeholder="1" /></label>
                </div>
              )}
              {formData.type === 'BUY_X_PAY_Y' && (
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                  <label className="field"><span>Cantidad a Comprar *</span><input type="text" value={formData.quantityToBuy || ''} onChange={(e) => { const val = e.target.value; handleFormChange('quantityToBuy', val === '' ? '' : Math.max(1, parseInt(val) || 1)); }} placeholder="1" /></label>
                  <label className="field"><span>Cantidad a Pagar *</span><input type="text" value={formData.quantityToGet || ''} onChange={(e) => { const val = e.target.value; handleFormChange('quantityToGet', val === '' ? '' : Math.max(1, parseInt(val) || 1)); }} placeholder="1" /></label>
                </div>
              )}

              {/* Fechas */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <label className="field"><span>Fecha Inicio</span><input type="date" value={formData.startDate} onChange={(e) => handleFormChange('startDate', e.target.value)} /></label>
                <label className="field"><span>Fecha Fin</span><input type="date" value={formData.endDate} onChange={(e) => handleFormChange('endDate', e.target.value)} /></label>
              </div>

              {/* Días Aplicables */}
              <label className="field"><span>Días Aplicables</span>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(120px, 1fr))', gap: '0.5rem', marginTop: '0.5rem' }}>
                  {['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'].map(day => (
                    <label key={day} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                      <input type="checkbox" checked={formData.applicableDays.has(day as DayOfWeek)} onChange={() => toggleDay(day as DayOfWeek)} />
                      <span>{['Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb', 'Dom'][['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'].indexOf(day)]}</span>
                    </label>
                  ))}
                </div>
              </label>

              {/* Estado */}
              <label className="field"><span>Estado</span>
                <div style={{ display: 'flex', gap: '1rem', marginTop: '0.5rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                    <input type="radio" checked={formData.active} onChange={() => handleFormChange('active', true)} />
                    <span>Activo</span>
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                    <input type="radio" checked={!formData.active} onChange={() => handleFormChange('active', false)} />
                    <span>Inactivo</span>
                  </label>
                </div>
              </label>

              <div className="modal__actions">
                <button type="button" className="btn btn--ghost" onClick={() => setAddDialogOpen(false)}>Cancelar</button>
                <button type="submit" className="btn btn--grad" disabled={formLoading}>{formLoading ? <Loader2 size={16} className="spin" /> : null} Crear</button>
              </div>
            </form>
          </div>
        </div>

        {/* Delete dialog */}
        <div className={`modal ${deleteDialog.open ? 'is-open' : ''}`} style={{ display: deleteDialog.open ? 'block' : 'none' }}>
          <div className="modal__backdrop" onClick={() => setDeleteDialog({ open: false, promotion: null })} />
          <div className="modal__card">
            <h2 className="modal__title">Eliminar Promoción</h2>
            <p className="modal__sub">¿Eliminar la promoción "{deleteDialog.promotion?.name}"?</p>
            <div className="modal__actions">
              <button className="btn btn--ghost" onClick={() => setDeleteDialog({ open: false, promotion: null })}>Cancelar</button>
              <button className="btn btn--danger" onClick={confirmDelete} disabled={formLoading}>{formLoading ? 'Eliminando...' : 'Eliminar'}</button>
            </div>
          </div>
        </div>

        {/* Toggle dialog */}
        <div className={`modal ${toggleDialog.open ? 'is-open' : ''}`} style={{ display: toggleDialog.open ? 'block' : 'none' }}>
          <div className="modal__backdrop" onClick={() => setToggleDialog({ open: false, promotion: null })} />
          <div className="modal__card">
            <h2 className="modal__title">{toggleDialog.promotion?.active ? 'Desactivar' : 'Activar'} Promoción</h2>
            <p className="modal__sub">¿Deseas {toggleDialog.promotion?.active ? 'desactivar' : 'activar'} "{toggleDialog.promotion?.name}"?</p>
            <div className="modal__actions">
              <button className="btn btn--ghost" onClick={() => setToggleDialog({ open: false, promotion: null })}>Cancelar</button>
              <button className={`btn ${toggleDialog.promotion?.active ? 'btn--warning' : 'btn--success'}`} onClick={confirmToggle} disabled={formLoading}>{formLoading ? 'Actualizando...' : (toggleDialog.promotion?.active ? 'Desactivar' : 'Activar')}</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
