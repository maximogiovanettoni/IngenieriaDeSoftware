import { useState, useEffect, useCallback } from "react";
import {
  Plus, PackagePlus, Trash2, ArrowLeft, Search, Filter, XCircle,
  Package, Info, Edit2, Loader2, AlertCircle
} from "lucide-react";
import { ingredientAPI, Ingredient, CreateIngredientDTO } from "@/services/ingredientAPI";
import { useToken } from "@/services/TokenContext";
import { useLocation } from "wouter";
import "./ingredients-screen.css";

interface IngredientesScreenProps {
  onNavigate?: (section: string) => void;
}
interface NewIngredientForm { name: string; unitMeasure: string; stock: string; }
interface FilterState { stockMin: string; stockMax: string; activeFilter: "all" | "true" | "false"; }

function Modal({
  isOpen, onClose, children, className = "",
}: { isOpen: boolean; onClose: () => void; children: React.ReactNode; className?: string }) {
  if (!isOpen) return null;
  return (
    <div className="modal">
      <div className="modal__backdrop" onClick={onClose} />
      <div className={`modal__card ${className}`}>{children}</div>
    </div>
  );
}

export default function IngredientsScreen({ onNavigate }: IngredientesScreenProps) {
  const [tokenState] = useToken();
  const [, setLocation] = useLocation();

  const [filters, setFilters] = useState<FilterState>({ stockMin: "", stockMax: "", activeFilter: "all" });
  const [searchTerm, setSearchTerm] = useState("");
  const [ingredients, setIngredients] = useState<Ingredient[]>([]);
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [addDialogOpen, setAddDialogOpen] = useState(false);
  const [infoDialog, setInfoDialog] = useState<{ open: boolean; ingredient: Ingredient | null }>({ open: false, ingredient: null });
  const [editNameDialog, setEditNameDialog] = useState<{ open: boolean; ingredient: Ingredient | null }>({ open: false, ingredient: null });
  const [addStockDialog, setAddStockDialog] = useState<{ open: boolean; ingredient: Ingredient | null }>({ open: false, ingredient: null });
  const [confirmAddStock, setConfirmAddStock] = useState(false);
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; ingredient: Ingredient | null }>({ open: false, ingredient: null });

  const [formData, setFormData] = useState<NewIngredientForm>({ name: "", unitMeasure: "", stock: "" });
  const [confirmAdd, setConfirmAdd] = useState(false);
  const [stockToAdd, setStockToAdd] = useState("");
  const [stockError, setStockError] = useState("");
  const [newName, setNewName] = useState("");
  const [nameError, setNameError] = useState("");
  const [deleteReason, setDeleteReason] = useState("");
  const [errors, setErrors] = useState<{ name?: string; stock?: string }>({});

  useEffect(() => {
    if (tokenState.state !== "LOGGED_IN") { setLocation("/login"); }
  }, [tokenState, setLocation]);

  const loadIngredients = useCallback(async () => {
    setLoading(true); setError(null);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      const data = await ingredientAPI.getAll(token);
      setIngredients(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error cargando ingredientes");
      if (err instanceof Error && err.message.includes("No autorizado")) setLocation("/login");
    } finally { setLoading(false); }
  }, [tokenState, setLocation]);

  useEffect(() => { if (tokenState.state === "LOGGED_IN") loadIngredients(); }, [tokenState, loadIngredients]);

  const handleNavigate = (section: string) => {
    if (onNavigate) onNavigate(section);
    else if (section === "admin") setLocation("/admin");
  };

  const filteredIngredients = ingredients.filter((ingredient) => {
    if (searchTerm && !ingredient.name.toLowerCase().includes(searchTerm.toLowerCase())) return false;
    if (filters.stockMin && ingredient.stock < parseFloat(filters.stockMin)) return false;
    if (filters.stockMax && ingredient.stock > parseFloat(filters.stockMax)) return false;
    if (filters.activeFilter === "true" && !ingredient.available) return false;
    if (filters.activeFilter === "false" && ingredient.available) return false;
    return true;
  });

  const validateForm = () => {
    const newErrors: typeof errors = {};
    if (ingredients.some((ing) => ing.name.toLowerCase() === formData.name.toLowerCase())) {
      newErrors.name = "Ya existe un ingrediente con este nombre";
    }
    const stock = parseFloat(formData.stock);
    if (stock < 0) newErrors.stock = "El stock inicial no puede ser negativo";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = () => {
    if (formData.name && formData.unitMeasure && formData.stock) if (validateForm()) setConfirmAdd(true);
  };

  const confirmAddIngredient = async () => {
    setLoading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      const dto: CreateIngredientDTO = { name: formData.name, unitMeasure: formData.unitMeasure, stock: parseFloat(formData.stock) };
      await ingredientAPI.create(dto, token);
      await loadIngredients();
      setFormData({ name: "", unitMeasure: "", stock: "" });
      setErrors({}); setConfirmAdd(false); setAddDialogOpen(false);
    } catch (err) { setError(err instanceof Error ? err.message : "Error creando ingrediente"); }
    finally { setLoading(false); }
  };

  const handleAddStock = () => {
    if (!addStockDialog.ingredient || !stockToAdd || parseFloat(stockToAdd) <= 0) return;
    if (addStockDialog.ingredient.unitMeasure === "unidad" && !Number.isInteger(parseFloat(stockToAdd))) {
      setStockError("Para unidades, solo se permiten cantidades enteras"); return;
    }
    setStockError(""); setConfirmAddStock(true);
  };

  const confirmAddStockAction = async () => {
    if (!addStockDialog.ingredient || !stockToAdd) return;
    setLoading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      await ingredientAPI.addStock(addStockDialog.ingredient.id, parseFloat(stockToAdd), token);
      await loadIngredients();
      setAddStockDialog({ open: false, ingredient: null }); setStockToAdd(""); setStockError(""); setConfirmAddStock(false);
    } catch (err) { setError(err instanceof Error ? err.message : "Error agregando stock"); setConfirmAddStock(false); }
    finally { setLoading(false); }
  };

  const handleDeactivate = (ingredient: Ingredient) => { setDeleteDialog({ open: true, ingredient }); setDeleteReason(""); };

  const confirmDeactivate = async () => {
    if (!deleteDialog.ingredient) return;
    setLoading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      await ingredientAPI.deactivate(deleteDialog.ingredient.id, deleteReason || undefined, token);
      await loadIngredients();
      setDeleteDialog({ open: false, ingredient: null }); setDeleteReason("");
    } catch (err) { setError(err instanceof Error ? err.message : "Error desactivando ingrediente"); }
    finally { setLoading(false); }
  };

  const handleEditName = async () => {
    if (!newName.trim()) { setNameError("El nombre no puede estar vacío"); return; }
    if (ingredients.some((ing) => ing.id !== editNameDialog.ingredient?.id && ing.name.toLowerCase() === newName.toLowerCase())) {
      setNameError("Ya existe un ingrediente con este nombre"); return;
    }
    if (!editNameDialog.ingredient) return;
    setLoading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      await ingredientAPI.changeName(editNameDialog.ingredient.id, newName, token);
      await loadIngredients();
      setEditNameDialog({ open: false, ingredient: null }); setNewName(""); setNameError("");
    } catch (err) { setError(err instanceof Error ? err.message : "Error modificando nombre"); }
    finally { setLoading(false); }
  };

  return (
    <div className="page">
      <div className="container">
        {/* Header */}
        <div className="header">
          <div className="badge"><Package size={28} className="badge__ico" /></div>
          <p className="header__title">Gestión de Ingredientes</p>
        </div>

        {/* Error */}
        {error && (
          <div className="alert alert--error">
            <AlertCircle size={18} />
            <span>{error}</span>
            <button className="alert__close" onClick={() => setError(null)}>
              <XCircle size={18} />
            </button>
          </div>
        )}

        {/* Card */}
        <section className="card card--glow">
          <div className="card__inner">
            <div className="card__head">
              <p className="card__title">Ingredientes</p>
              <button className="btn btn--grad" onClick={() => setAddDialogOpen(true)} disabled={loading}>
                <Plus size={16} /> <span>Agregar Ingrediente</span>
              </button>
            </div>

            {/* Search + Filters */}
            <div className="filters">
              <div className="input input--with-ico">
                <Search size={16} className="input__ico" />
                <input
                  type="text" placeholder="Buscar ingredientes…"
                  value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <div className="filters__right">
                <button className="btn btn--outline" onClick={() => setIsFilterOpen((v) => !v)}>
                  <Filter size={16} /> <span>Filtros</span>
                </button>

                {isFilterOpen && (
                  <div className="filter-pop">
                    <h3>Filtros</h3>

                    <div className="field">
                      <label>Rango de Stock</label>
                      <div className="row">
                        <input
                          type="number" placeholder="Mínimo" value={filters.stockMin}
                          onChange={(e) => setFilters((p) => ({ ...p, stockMin: e.target.value }))}
                        />
                        <span className="dash">-</span>
                        <input
                          type="number" placeholder="Máximo" value={filters.stockMax}
                          onChange={(e) => setFilters((p) => ({ ...p, stockMax: e.target.value }))}
                        />
                      </div>
                    </div>

                    <div className="field">
                      <label>Estado</label>
                      <div className="row">
                        {(["all", "true", "false"] as const).map((v) => (
                          <button
                            key={v}
                            className={`chip ${filters.activeFilter === v ? "chip--active" : ""}`}
                            onClick={() => setFilters((p) => ({ ...p, activeFilter: v }))}
                          >
                            {v === "all" ? "Todos" : v === "true" ? "Activo" : "Inactivo"}
                          </button>
                        ))}
                      </div>
                    </div>

                    <button
                      className="btn btn--ghost w-full"
                      onClick={() => setFilters({ stockMin: "", stockMax: "", activeFilter: "all" })}
                    >
                      Limpiar Filtros
                    </button>
                  </div>
                )}
              </div>
            </div>

            {/* Table */}
            <div className="table">
              <div className="table__scroll">
                <table>
                  <thead>
                    <tr>
                      <th>Nombre</th>
                      <th className="hide-md">Unidad</th>
                      <th className="hide-md">Stock</th>
                      <th className="hide-md text-center">Estado</th>
                      <th className="text-right">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {loading ? (
                      <tr><td colSpan={5} className="table__center muted"><Loader2 size={16} className="spin" /> Cargando ingredientes…</td></tr>
                    ) : filteredIngredients.length === 0 ? (
                      <tr><td colSpan={5} className="table__center muted">No se encontraron ingredientes con los filtros aplicados</td></tr>
                    ) : (
                      filteredIngredients.map((ingredient) => (
                        <tr key={ingredient.id}>
                          <td>{ingredient.name}</td>
                          <td className="muted hide-md">{ingredient.unitMeasure}</td>
                          <td className="muted hide-md">{ingredient.stock}</td>
                          <td className="muted hide-md text-center">
                            {ingredient.available ? (
                              <span className="pill pill--ok">Activo</span>
                            ) : (
                              <span className="pill pill--bad">Inactivo</span>
                            )}
                          </td>
                          <td className="text-right">
                            <div className="row-end">
                              <button className="icon-btn info md-hide" onClick={() => setInfoDialog({ open: true, ingredient })} title="Ver información">
                                <Info size={14} />
                              </button>
                              <button className="icon-btn info" onClick={() => { setEditNameDialog({ open: true, ingredient }); setNewName(ingredient.name); }} title="Modificar nombre">
                                <Edit2 size={14} />
                              </button>
                              <button className="icon-btn info" onClick={() => { setAddStockDialog({ open: true, ingredient }); setStockToAdd(""); setStockError(""); }} title="Modificar Stock">
                                <PackagePlus size={14} />
                              </button>
                              <button className="icon-btn danger" onClick={() => handleDeactivate(ingredient)} title="Desactivar" disabled={!ingredient.available}>
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

            <button className="back" onClick={() => handleNavigate("admin")}>
              <ArrowLeft size={14} /> Volver a Menú Inicial
            </button>
          </div>
        </section>
      </div>

      {/* Info */}
      <Modal isOpen={infoDialog.open} onClose={() => setInfoDialog({ open: false, ingredient: null })}>
        <h2 className="modal__title">Información del Ingrediente</h2>
        {infoDialog.ingredient && (
          <div className="space">
            {/* Nombre */}
            <div className="info-modal__section">
              <label className="info-modal__label">Nombre</label>
              <p className="info-modal__text">{infoDialog.ingredient.name}</p>
            </div>

            {/* Grid: Unidad y Stock */}
            <div className="info-modal__grid">
              <div className="info-modal__panel">
                <label className="info-modal__label">Unidad</label>
                <p className="info-modal__text">{infoDialog.ingredient.unitMeasure}</p>
              </div>

              <div className="info-modal__panel">
                <label className="info-modal__label">Stock</label>
                <p className="info-modal__text">{infoDialog.ingredient.stock}</p>
              </div>
            </div>

            {/* Estado */}
            <div className="info-modal__panel">
              <label className="info-modal__label">Estado</label>
              <span className={`info-modal__status ${infoDialog.ingredient.available ? 'info-modal__status--active' : 'info-modal__status--inactive'}`}>
                {infoDialog.ingredient.available ? "Activo" : "Inactivo"}
              </span>
            </div>

            {/* Botón */}
            <div className="info-modal__actions">
              <button 
                className="btn btn--grad"
                onClick={() => setInfoDialog({ open: false, ingredient: null })}
              >
                Cerrar
              </button>
            </div>
          </div>
        )}
      </Modal>

      {/* Edit name */}
      <Modal
        isOpen={editNameDialog.open}
        onClose={() => { setEditNameDialog({ open: false, ingredient: null }); setNewName(""); setNameError(""); }}
      >
        <h2 className="modal__title">Modificar Nombre</h2>
        <p className="modal__sub">Modificando: {editNameDialog.ingredient?.name}</p>
        <label className="field">
          <span>Nuevo Nombre</span>
          <input
            value={newName}
            onChange={(e) => { setNewName(e.target.value); setNameError(""); }}
            placeholder="Ingrese nuevo nombre"
          />
          {nameError && <small className="field__error">{nameError}</small>}
        </label>
        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => { setEditNameDialog({ open: false, ingredient: null }); setNewName(""); setNameError(""); }}>
            Cancelar
          </button>
          <button className="btn btn--grad" onClick={handleEditName} disabled={!newName.trim() || loading}>
            {loading ? <Loader2 size={16} className="spin" /> : null} Guardar
          </button>
        </div>
      </Modal>

      {/* Add stock */}
      <Modal
        isOpen={addStockDialog.open && !confirmAddStock}
        onClose={() => { setAddStockDialog({ open: false, ingredient: null }); setStockError(""); }}
      >
        <h2 className="modal__title">Modificar Stock</h2>
        <p className="modal__sub">A: {addStockDialog.ingredient?.name}</p>
        <label className="field">
          <span>Nuevo valor de stock</span>
          <input
            type="number"
            min="0.01"
            step={addStockDialog.ingredient?.unitMeasure === "unidad" ? "1" : "0.01"}
            value={stockToAdd}
            onChange={(e) => { setStockToAdd(e.target.value); setStockError(""); }}
            placeholder="Ingrese nuevo stock"
          />
          {stockError && <small className="field__error">{stockError}</small>}
          <small className="field__hint">
            Stock actual: {addStockDialog.ingredient?.stock} {addStockDialog.ingredient?.unitMeasure}
          </small>
          {stockToAdd && parseFloat(stockToAdd) > 0 && !stockError && (
            <small className="field__hint blue">
              Nuevo stock: {parseFloat(stockToAdd)} {addStockDialog.ingredient?.unitMeasure}
            </small>
          )}
        </label>
        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => { setAddStockDialog({ open: false, ingredient: null }); setStockError(""); }}>
            Cancelar
          </button>
          <button className="btn btn--grad" onClick={handleAddStock} disabled={!stockToAdd || parseFloat(stockToAdd) <= 0 || !!stockError}>
            Modificar
          </button>
        </div>
      </Modal>

      {/* Confirm add stock */}
      <Modal isOpen={confirmAddStock} onClose={() => setConfirmAddStock(false)}>
        <h2 className="modal__title">Confirmar Operación</h2>
        <p className="modal__sub">
          ¿Modificar stock a {stockToAdd} {addStockDialog.ingredient?.unitMeasure} para "{addStockDialog.ingredient?.name}"?
          <br />
          Pasará de {addStockDialog.ingredient?.stock} a {parseFloat(stockToAdd || "0")}.
        </p>
        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setConfirmAddStock(false)} disabled={loading}>
            Cancelar
          </button>
          <button className="btn btn--grad" onClick={confirmAddStockAction} disabled={loading}>
            {loading ? <Loader2 size={16} className="spin" /> : null} Confirmar
          </button>
        </div>
      </Modal>

      {/* Delete / Deactivate Dialog — USA confirmDeactivate */}
      <Modal
        isOpen={deleteDialog.open}
        onClose={() => setDeleteDialog({ open: false, ingredient: null })}
      >
        <h2 className="modal__title">Confirmar Desactivación</h2>
        <p className="modal__sub">
          ¿Está seguro que desea desactivar el ingrediente "
          {deleteDialog.ingredient?.name}"? El ingrediente quedará marcado como inactivo.
        </p>

        <label className="field">
          <span>Razón (opcional)</span>
          <textarea
            rows={3}
            value={deleteReason}
            onChange={(e) => setDeleteReason(e.target.value)}
            placeholder="Ingrese la razón de desactivación…"
          />
        </label>

        <div className="modal__actions">
          <button
            className="btn btn--ghost"
            onClick={() => setDeleteDialog({ open: false, ingredient: null })}
            disabled={loading}
          >
            Cancelar
          </button>
          <button
            className="btn btn--grad"
            onClick={confirmDeactivate}   // <— se usa aquí
            disabled={loading}
          >
            {loading ? <Loader2 size={16} className="spin" /> : null}
            Desactivar
          </button>
        </div>
      </Modal>

      {/* Create ingredient */}
      <Modal isOpen={addDialogOpen && !confirmAdd} onClose={() => setAddDialogOpen(false)}>
        <h2 className="modal__title">Agregar Nuevo Ingrediente</h2>
        <p className="modal__sub">Complete todos los campos requeridos</p>
        <div className="space">
          <label className="field">
            <span>Nombre *</span>
            <input
              value={formData.name}
              onChange={(e) => { setFormData((p) => ({ ...p, name: e.target.value })); setErrors((p) => ({ ...p, name: undefined })); }}
              placeholder="Ej: Harina de Trigo"
            />
            {errors.name && <small className="field__error">{errors.name}</small>}
          </label>

          <label className="field">
            <span>Unidad de Medida *</span>
            <input
              value={formData.unitMeasure}
              onChange={(e) => setFormData((p) => ({ ...p, unitMeasure: e.target.value }))}
              placeholder="Ej: kg, litro, unidad"
            />
          </label>

          <label className="field">
            <span>Stock Inicial *</span>
            <input
              type="number" min="0" step="0.01" value={formData.stock}
              onChange={(e) => { setFormData((p) => ({ ...p, stock: e.target.value })); setErrors((p) => ({ ...p, stock: undefined })); }}
              placeholder="0"
            />
            {errors.stock && <small className="field__error">{errors.stock}</small>}
          </label>
        </div>

        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setAddDialogOpen(false)} disabled={loading}>
            Cancelar
          </button>
          <button
            className="btn btn--grad"
            onClick={handleSubmit}
            disabled={!formData.name || !formData.unitMeasure || !formData.stock || loading}
          >
            Crear Ingrediente
          </button>
        </div>
      </Modal>

      {/* Confirm create */}
      <Modal isOpen={confirmAdd} onClose={() => setConfirmAdd(false)}>
        <h2 className="modal__title">Confirmar Nuevo Ingrediente</h2>
        <p className="modal__sub">
          ¿Crear "{formData.name}"?<br />
          Stock inicial: {formData.stock} {formData.unitMeasure}
        </p>
        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setConfirmAdd(false)} disabled={loading}>
            Cancelar
          </button>
          <button className="btn btn--grad" onClick={confirmAddIngredient} disabled={loading}>
            {loading ? <Loader2 size={16} className="spin" /> : null} Confirmar
          </button>
        </div>
      </Modal>
    </div>
  );
}

