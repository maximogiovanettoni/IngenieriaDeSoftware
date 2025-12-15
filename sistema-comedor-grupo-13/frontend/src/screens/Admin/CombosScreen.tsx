import {
  AlertCircle,
  ArchiveRestore,
  ArrowLeft,
  DollarSign,
  Edit,
  Filter,
  Image as ImageIcon,
  Info,
  Layers,
  Plus,
  Search,
  Trash2,
  X,
} from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useLocation } from "wouter";

import { Combo, CreateComboDTO, comboService } from "@/services/ComboServices";
import { Product, productAPI } from "@/services/ProductServices";
import { useToken } from "@/services/TokenContext";

import { ImageUploader } from "../../components/Admin/ImageUploader";
import "../../components/Admin/image-uploader.css";
import "./combos-screen.css";

type ModalType =
  | "create"
  | "editName"
  | "editPrice"
  | "editImage"
  | "delete"
  | "viewProducts"
  | "filters"
  | "restore"
  | null;

interface CombosScreenProps {
  onNavigate?: (section: string) => void;
}

export default function CombosScreen({ onNavigate }: CombosScreenProps) {
  const [tokenState] = useToken();
  const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
  const [, setLocation] = useLocation();

  const [combos, setCombos] = useState<Combo[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const [searchTerm, setSearchTerm] = useState("");
  const [minPrice, setMinPrice] = useState<string>("");
  const [maxPrice, setMaxPrice] = useState<string>("");
  const [selectedProductIds, setSelectedProductIds] = useState<number[]>([]);

  const [tempMinPrice, setTempMinPrice] = useState<string>("");
  const [tempMaxPrice, setTempMaxPrice] = useState<string>("");
  const [tempSelectedProductIds, setTempSelectedProductIds] = useState<number[]>([]);

  const [modalType, setModalType] = useState<ModalType>(null);
  const [selectedCombo, setSelectedCombo] = useState<Combo | null>(null);

  const [formName, setFormName] = useState("");
  const [formDescription, setFormDescription] = useState("");
  const [formPrice, setFormPrice] = useState("");
  const [formActive, setFormActive] = useState(true);
  const [selectedProducts, setSelectedProducts] = useState<Record<number, number>>({});

  const [selectedImage, setSelectedImage] = useState<File | null>(null);
  const [creationImageFile, setCreationImageFile] = useState<File | null>(null);
  const [creationImagePreview, setCreationImagePreview] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const [combosData, productsData] = await Promise.all([comboService.getAll(token), productAPI.getAll(token)]);
      setCombos(combosData);
      setProducts(productsData.filter((p) => p.available));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error cargando datos");
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const filteredCombos = useMemo(() => {
    return combos.filter((combo) => {
      if (searchTerm && !combo.name.toLowerCase().includes(searchTerm.toLowerCase())) {
        return false;
      }

      const min = minPrice ? parseFloat(minPrice) : 0;
      const max = maxPrice ? parseFloat(maxPrice) : Infinity;

      if (minPrice && combo.price < min) return false;
      if (maxPrice && combo.price > max) return false;

      if (selectedProductIds.length > 0) {
        const comboProductIds = combo.products.map((p) => p.id);
        const hasAllProducts = selectedProductIds.every((productId) => comboProductIds.includes(productId));
        if (!hasAllProducts) return false;
      }

      return true;
    });
  }, [combos, searchTerm, minPrice, maxPrice, selectedProductIds]);

  const showSuccess = (message: string) => {
    setSuccessMessage(message);
    setTimeout(() => setSuccessMessage(null), 3000);
  };

  const handleNavigate = (section: string) => {
    if (onNavigate) onNavigate(section);
    else if (section === "admin") setLocation("/admin");
  };

  const openCreateModal = () => {
    setFormName("");
    setFormDescription("");
    setFormPrice("");
    setCreationImageFile(null);
    setCreationImagePreview(null);
    setFormActive(true);
    setSelectedProducts({});
    setModalType("create");
  };

  const openEditImageModal = (combo: Combo) => {
    setSelectedCombo(combo);
    setSelectedImage(null);
    setModalType("editImage");
  };

  const openEditNameModal = (combo: Combo) => {
    setSelectedCombo(combo);
    setFormName(combo.name);
    setModalType("editName");
  };

  const openEditPriceModal = (combo: Combo) => {
    setSelectedCombo(combo);
    setFormPrice(combo.price.toString());
    setModalType("editPrice");
  };

  const openDeleteModal = (combo: Combo) => {
    setSelectedCombo(combo);
    setModalType("delete");
  };

  const openRestoreModal = (combo: Combo) => {
    setSelectedCombo(combo);
    setModalType("restore");
  };

  const openViewProductsModal = (combo: Combo) => {
    setSelectedCombo(combo);
    setModalType("viewProducts");
  };

  const openFiltersModal = () => {
    setTempMinPrice(minPrice);
    setTempMaxPrice(maxPrice);
    setTempSelectedProductIds([...selectedProductIds]);
    setModalType("filters");
  };

  const applyFilters = () => {
    setMinPrice(tempMinPrice);
    setMaxPrice(tempMaxPrice);
    setSelectedProductIds([...tempSelectedProductIds]);
    setModalType(null);
  };

  const clearFilters = () => {
    setTempMinPrice("");
    setTempMaxPrice("");
    setTempSelectedProductIds([]);
  };

  const clearAllFilters = () => {
    setMinPrice("");
    setMaxPrice("");
    setSelectedProductIds([]);
    setTempMinPrice("");
    setTempMaxPrice("");
    setTempSelectedProductIds([]);
  };

  const toggleProductInFilter = (productId: number) => {
    setTempSelectedProductIds((prev) => {
      if (prev.includes(productId)) {
        return prev.filter((id) => id !== productId);
      } else {
        return [...prev, productId];
      }
    });
  };

  const closeModal = () => {
    setModalType(null);
    setSelectedCombo(null);
    setError(null);
    setSelectedImage(null);
    setCreationImageFile(null);
    setCreationImagePreview(null);
  };

  const handleCreateCombo = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formName.trim()) {
      setError("El nombre es obligatorio");
      return;
    }

    if (!formPrice || parseFloat(formPrice) <= 0) {
      setError("El precio debe ser mayor a 0");
      return;
    }

    if (Object.keys(selectedProducts).length === 0) {
      setError("Debe seleccionar al menos un producto");
      return;
    }

    try {
      const dto: CreateComboDTO = {
        name: formName.trim(),
        description: formDescription.trim() || undefined,
        price: parseFloat(formPrice),
        active: formActive,
        products: selectedProducts,
      };

      const createdCombo = await comboService.create(dto, token);

      // Subir imagen si se seleccionó una
      if (creationImageFile) {
        try {
          await comboService.updateImage(createdCombo.id, creationImageFile, token);
        } catch (imageErr) {
          console.warn("Error subiendo imagen, pero el combo fue creado:", imageErr);
        }
      }

      showSuccess("Combo creado exitosamente");
      closeModal();
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error creando combo");
    }
  };

  const handleUpdateName = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCombo) return;
    setError(null);

    if (!formName.trim()) {
      setError("El nombre es obligatorio");
      return;
    }

    try {
      await comboService.updateName(selectedCombo.id, formName.trim(), token);
      showSuccess("Nombre actualizado exitosamente");
      closeModal();
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error actualizando nombre");
    }
  };

  const handleUpdatePrice = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCombo) return;
    setError(null);

    if (!formPrice || parseFloat(formPrice) <= 0) {
      setError("El precio debe ser mayor a 0");
      return;
    }

    try {
      await comboService.updatePrice(selectedCombo.id, parseFloat(formPrice), token);
      showSuccess("Precio actualizado exitosamente");
      closeModal();
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error actualizando precio");
    }
  };

  const handleUpdateImage = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCombo) return;
    setError(null);

    if (!selectedImage) {
      setError("Debe seleccionar una imagen");
      return;
    }

    try {
      await comboService.updateImage(selectedCombo.id, selectedImage, token);
      showSuccess("Imagen actualizada exitosamente");
      closeModal();
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error actualizando imagen");
    }
  };

  const handleDeleteCombo = async () => {
    if (!selectedCombo) return;
    setError(null);

    try {
      await comboService.deactivate(selectedCombo.id, token);
      showSuccess("Combo desactivado exitosamente");
      closeModal();
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error desactivando combo");
    }
  };

  const handleRestoreCombo = async () => {
    if (!selectedCombo) return;
    setError(null);

    try {
      await comboService.restore(selectedCombo.id, token);
      showSuccess("Combo restaurado exitosamente");
      closeModal();
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error restaurando combo");
    }
  };

  const toggleProductSelection = (productId: number) => {
    setSelectedProducts((prev) => {
      const newSelection = { ...prev };
      if (newSelection[productId]) {
        delete newSelection[productId];
      } else {
        newSelection[productId] = 1;
      }
      return newSelection;
    });
  };

  const updateProductQuantity = (productId: number, quantity: number) => {
    if (quantity <= 0) {
      setSelectedProducts((prev) => {
        const newSelection = { ...prev };
        delete newSelection[productId];
        return newSelection;
      });
    } else {
      setSelectedProducts((prev) => ({
        ...prev,
        [productId]: quantity,
      }));
    }
  };

  const calculateRegularPrice = (): number => {
    return Object.entries(selectedProducts).reduce((total, [productId, quantity]) => {
      const product = products.find((p) => p.id === parseInt(productId));
      return total + (product ? product.price * quantity : 0);
    }, 0);
  };

  return (
    <div className="page">
      <div className="container">
        <div className="header">
          <div className="badge">
            <Layers size={28} className="badge__ico" />
          </div>
          <p className="header__title">Gestión de Combos</p>
        </div>

        {successMessage && (
          <div
            className="alert"
            style={{ background: "rgba(34,197,94,.2)", borderColor: "rgba(34,197,94,.5)", color: "#86efac" }}
          >
            <span>{successMessage}</span>
            <button className="alert__close" onClick={() => setSuccessMessage(null)}>
              <X size={16} />
            </button>
          </div>
        )}

        <section className="card card--glow">
          <div className="card__inner">
            {/* HEADER CON TÍTULO Y BOTÓN AGREGAR */}
            <div className="card__head">
              <p className="card__title">Combos</p>
              <button className="btn btn--grad" onClick={openCreateModal}>
                <Plus size={18} />
                <span>Agregar Combo</span>
              </button>
            </div>

            {/* BARRA DE BÚSQUEDA Y FILTROS EN LA MISMA LÍNEA */}
            <div style={{ display: "flex", gap: "12px", marginBottom: "16px", alignItems: "center" }}>
              <div className="input">
                <Search size={16} className="input__ico" />
                <input
                  type="text"
                  placeholder="Buscar combos..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>
              <button
                className="btn"
                style={{ background: "var(--panel-2)", color: "#fff", flexShrink: 0 }}
                onClick={openFiltersModal}
              >
                <Filter size={18} />
                <span>Filtros</span>
                {(minPrice || maxPrice || selectedProductIds.length > 0) && (
                  <span
                    style={{
                      background: "var(--blue-2)",
                      color: "#fff",
                      borderRadius: "12px",
                      padding: "2px 8px",
                      fontSize: "11px",
                      marginLeft: "4px",
                    }}
                  >
                    {[minPrice, maxPrice, ...selectedProductIds].filter(Boolean).length}
                  </span>
                )}
              </button>
            </div>

            {/* Active Filters Summary */}
            {(searchTerm || minPrice || maxPrice || selectedProductIds.length > 0) && (
              <div
                style={{ display: "flex", gap: "8px", flexWrap: "wrap", marginBottom: "16px", alignItems: "center" }}
              >
                <span className="muted" style={{ fontSize: "13px" }}>
                  Filtros activos:
                </span>
                {searchTerm && (
                  <span
                    style={{
                      background: "rgba(21,93,252,.2)",
                      color: "var(--blue-2)",
                      padding: "4px 10px",
                      borderRadius: "6px",
                      fontSize: "12px",
                      display: "flex",
                      alignItems: "center",
                      gap: "6px",
                    }}
                  >
                    Búsqueda: {searchTerm}
                    <X size={14} style={{ cursor: "pointer" }} onClick={() => setSearchTerm("")} />
                  </span>
                )}
                {minPrice && (
                  <span
                    style={{
                      background: "rgba(21,93,252,.2)",
                      color: "var(--blue-2)",
                      padding: "4px 10px",
                      borderRadius: "6px",
                      fontSize: "12px",
                      display: "flex",
                      alignItems: "center",
                      gap: "6px",
                    }}
                  >
                    Precio mín: ${minPrice}
                    <X size={14} style={{ cursor: "pointer" }} onClick={() => setMinPrice("")} />
                  </span>
                )}
                {maxPrice && (
                  <span
                    style={{
                      background: "rgba(21,93,252,.2)",
                      color: "var(--blue-2)",
                      padding: "4px 10px",
                      borderRadius: "6px",
                      fontSize: "12px",
                      display: "flex",
                      alignItems: "center",
                      gap: "6px",
                    }}
                  >
                    Precio máx: ${maxPrice}
                    <X size={14} style={{ cursor: "pointer" }} onClick={() => setMaxPrice("")} />
                  </span>
                )}
                {selectedProductIds.map((productId) => {
                  const product = products.find((p) => p.id === productId);
                  return product ? (
                    <span
                      key={productId}
                      style={{
                        background: "rgba(21,93,252,.2)",
                        color: "var(--blue-2)",
                        padding: "4px 10px",
                        borderRadius: "6px",
                        fontSize: "12px",
                        display: "flex",
                        alignItems: "center",
                        gap: "6px",
                      }}
                    >
                      Producto: {product.name}
                      <X
                        size={14}
                        style={{ cursor: "pointer" }}
                        onClick={() => setSelectedProductIds((prev) => prev.filter((id) => id !== productId))}
                      />
                    </span>
                  ) : null;
                })}
                <button
                  onClick={clearAllFilters}
                  style={{
                    background: "transparent",
                    border: "1px solid var(--border)",
                    color: "var(--muted)",
                    padding: "4px 10px",
                    borderRadius: "6px",
                    fontSize: "12px",
                    cursor: "pointer",
                  }}
                >
                  Limpiar todo
                </button>
              </div>
            )}

            {loading ? (
              <div className="table__center muted">Cargando...</div>
            ) : (
              <div className="table">
                <div className="table__scroll">
                  <table>
                    <thead>
                      <tr>
                        <th>Nombre</th>
                        <th className="hide-md">Precio</th>
                        <th className="hide-md">Precio Regular</th>
                        <th className="hide-md text-center">Disponible</th>
                        <th className="hide-md text-center">Estado</th>
                        <th className="text-right">Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {filteredCombos.length === 0 ? (
                        <tr>
                          <td colSpan={6} className="table__center muted">
                            No se encontraron combos
                          </td>
                        </tr>
                      ) : (
                        filteredCombos.map((combo) => (
                          <tr key={combo.id}>
                            <td>
                              <div>
                                <div>{combo.name}</div>
                                {combo.description && <div className="muted">{combo.description}</div>}
                              </div>
                            </td>
                            <td className="hide-md">
                              ${(typeof combo.price === "string" ? parseFloat(combo.price) : combo.price).toFixed(2)}
                              {combo.discount > 0 && (
                                <div className="muted" style={{ fontSize: "10px" }}>
                                  Ahorro: $
                                  {(typeof combo.discount === "string"
                                    ? parseFloat(combo.discount)
                                    : combo.discount
                                  ).toFixed(2)}
                                </div>
                              )}
                            </td>
                            <td className="hide-md muted">
                              $
                              {(typeof combo.regularPrice === "string"
                                ? parseFloat(combo.regularPrice)
                                : combo.regularPrice
                              ).toFixed(2)}
                            </td>
                            <td className="hide-md text-center">
                              <span className={combo.isAvailable ? "pill pill--ok" : "pill pill--bad"}>
                                {combo.isAvailable ? "Sí" : "No"}
                              </span>
                            </td>
                            <td className="hide-md text-center">
                              <span className={combo.isActive ? "pill pill--ok" : "pill pill--bad"}>
                                {combo.isActive ? "Activo" : "Inactivo"}
                              </span>
                            </td>
                            <td className="text-right">
                              <div className="row-end">
                                <button
                                  className="icon-btn info"
                                  onClick={() => openViewProductsModal(combo)}
                                  title="Ver productos"
                                >
                                  <Info size={16} />
                                </button>
                                <button
                                  className="icon-btn info"
                                  onClick={() => openEditNameModal(combo)}
                                  title="Editar nombre"
                                >
                                  <Edit size={16} />
                                </button>
                                <button
                                  className="icon-btn info"
                                  onClick={() => openEditPriceModal(combo)}
                                  title="Editar precio"
                                >
                                  <DollarSign size={16} />
                                </button>
                                <button
                                  className="icon-btn info"
                                  onClick={() => openEditImageModal(combo)}
                                  title="Editar imagen"
                                >
                                  <ImageIcon size={16} />
                                </button>
                                <button
                                  className="icon-btn danger"
                                  onClick={() => openDeleteModal(combo)}
                                  disabled={!combo.isActive}
                                  title="Desactivar"
                                >
                                  <Trash2 size={16} />
                                </button>
                                <button
                                  className="icon-btn success"
                                  onClick={() => openRestoreModal(combo)}
                                  disabled={combo.isActive}
                                  title="Restaurar"
                                >
                                  <ArchiveRestore size={16} />
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
            )}

            <button className="back" onClick={() => handleNavigate("admin")}>
              <ArrowLeft size={14} /> Volver a Menú Inicial
            </button>
          </div>
        </section>
      </div>

      {/* CREATE COMBO MODAL */}
      {modalType === "create" && (
        <div className="modal">
          <div className="modal__backdrop" onClick={closeModal} />
          <div className="modal__card" style={{ maxWidth: "640px" }}>
            <h2 className="modal__title">Crear Combo</h2>
            <p className="modal__sub">Complete la información del nuevo combo</p>

            {error && (
              <div className="alert">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleCreateCombo}>
              <div className="space">
                <div className="field">
                  <label>Nombre *</label>
                  <input
                    type="text"
                    value={formName}
                    onChange={(e) => setFormName(e.target.value)}
                    placeholder="Ej: Combo Completo"
                    maxLength={100}
                  />
                </div>

                <div className="field">
                  <label>Descripción</label>
                  <textarea
                    value={formDescription}
                    onChange={(e) => setFormDescription(e.target.value)}
                    placeholder="Descripción del combo"
                    maxLength={300}
                  />
                </div>

                <div className="field">
                  <span>Imagen del Combo</span>
                  <ImageUploader
                    onFileSelect={(file) => {
                      setCreationImageFile(file);
                      setCreationImagePreview(URL.createObjectURL(file));
                    }}
                    previewUrl={creationImagePreview}
                    error={error}
                  />
                </div>

                <label className="field field--row">
                  <input type="checkbox" checked={formActive} onChange={(e) => setFormActive(e.target.checked)} />
                  <span>Activo</span>
                </label>
                <div className="field">
                  <label>Productos * (Selecciona y define cantidades)</label>
                  <div
                    style={{
                      maxHeight: "300px",
                      overflow: "auto",
                      border: "1px solid var(--border)",
                      borderRadius: "8px",
                      padding: "12px",
                      background: "rgba(38,38,38,.3)",
                    }}
                  >
                    {products.length === 0 ? (
                      <p className="muted">No hay productos disponibles</p>
                    ) : (
                      products.map((product) => (
                        <div
                          key={product.id}
                          style={{
                            display: "grid",
                            gridTemplateColumns: "auto 1fr auto",
                            alignItems: "center",
                            gap: "12px",
                            marginBottom: "10px",
                            padding: "10px 12px",
                            background: selectedProducts[product.id] ? "rgba(21,93,252,.15)" : "transparent",
                            borderRadius: "6px",
                            border: `1px solid ${selectedProducts[product.id] ? "rgba(21,93,252,.5)" : "var(--border)"}`,
                            cursor: "pointer",
                          }}
                          onClick={() => toggleProductSelection(product.id)}
                        >
                          <input
                            type="checkbox"
                            checked={!!selectedProducts[product.id]}
                            onChange={() => toggleProductSelection(product.id)}
                            onClick={(e) => e.stopPropagation()}
                            style={{ cursor: "pointer", width: "16px", height: "16px" }}
                          />
                          <div
                            style={{
                              display: "flex",
                              flexDirection: "column",
                              minWidth: 0,
                            }}
                          >
                            <span
                              style={{
                                color: "#fff",
                                fontSize: "14px",
                                fontWeight: selectedProducts[product.id] ? "500" : "normal",
                              }}
                            >
                              {product.name}
                            </span>
                            <span
                              style={{
                                color: "var(--muted)",
                                fontSize: "12px",
                              }}
                            >
                              $
                              {(typeof product.price === "string" ? parseFloat(product.price) : product.price).toFixed(
                                2,
                              )}
                            </span>
                          </div>
                          {selectedProducts[product.id] && (
                            <input
                              type="number"
                              min="1"
                              value={selectedProducts[product.id]}
                              onChange={(e) => {
                                e.stopPropagation();
                                updateProductQuantity(product.id, parseInt(e.target.value) || 0);
                              }}
                              onClick={(e) => e.stopPropagation()}
                              style={{
                                width: "70px",
                                height: "32px",
                                padding: "4px 8px",
                                background: "rgba(38,38,38,.8)",
                                border: "1px solid var(--border)",
                                borderRadius: "6px",
                                color: "#fff",
                                textAlign: "center",
                                fontSize: "14px",
                              }}
                            />
                          )}
                        </div>
                      ))
                    )}
                  </div>
                  {Object.keys(selectedProducts).length > 0 && (
                    <span className="field__hint blue">
                      Precio regular calculado: ${calculateRegularPrice().toFixed(2)}
                    </span>
                  )}
                </div>

                <div className="field">
                  <label>Precio del Combo * (Debe ser menor o igual al precio regular)</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={formPrice}
                    onChange={(e) => setFormPrice(e.target.value)}
                    placeholder="0.00"
                  />
                  {formPrice && calculateRegularPrice() > 0 && (
                    <span className="field__hint blue">
                      Descuento: ${(calculateRegularPrice() - parseFloat(formPrice || "0")).toFixed(2)}
                    </span>
                  )}
                </div>
              </div>

              <div className="modal__actions">
                <button type="button" className="btn btn--ghost" onClick={closeModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn--grad">
                  Crear Combo
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT NAME MODAL */}
      {modalType === "editName" && selectedCombo && (
        <div className="modal">
          <div className="modal__backdrop" onClick={closeModal} />
          <div className="modal__card">
            <h2 className="modal__title">Editar Nombre</h2>
            <p className="modal__sub">Modificar el nombre del combo</p>

            {error && (
              <div className="alert">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleUpdateName}>
              <div className="field">
                <label>Nombre *</label>
                <input
                  type="text"
                  value={formName}
                  onChange={(e) => setFormName(e.target.value)}
                  placeholder="Nombre del combo"
                  maxLength={100}
                  autoFocus
                />
              </div>

              <div className="modal__actions">
                <button type="button" className="btn btn--ghost" onClick={closeModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn--grad">
                  Guardar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT PRICE MODAL */}
      {modalType === "editPrice" && selectedCombo && (
        <div className="modal">
          <div className="modal__backdrop" onClick={closeModal} />
          <div className="modal__card">
            <h2 className="modal__title">Editar Precio</h2>
            <p className="modal__sub">Modificar el precio del combo</p>

            {error && (
              <div className="alert">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleUpdatePrice}>
              <div className="field">
                <label>Precio *</label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={formPrice}
                  onChange={(e) => setFormPrice(e.target.value)}
                  placeholder="0.00"
                  autoFocus
                />
                <span className="field__hint">
                  Precio regular: $
                  {(typeof selectedCombo.regularPrice === "string"
                    ? parseFloat(selectedCombo.regularPrice)
                    : selectedCombo.regularPrice
                  ).toFixed(2)}
                </span>
                {formPrice && (
                  <span className="field__hint blue">
                    Nuevo descuento: $
                    {(
                      (typeof selectedCombo.regularPrice === "string"
                        ? parseFloat(selectedCombo.regularPrice)
                        : selectedCombo.regularPrice) - parseFloat(formPrice || "0")
                    ).toFixed(2)}
                  </span>
                )}
              </div>

              <div className="modal__actions">
                <button type="button" className="btn btn--ghost" onClick={closeModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn--grad">
                  Guardar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT IMAGE MODAL */}
      {modalType === "editImage" && selectedCombo && (
        <div className="modal">
          <div className="modal__backdrop" onClick={closeModal} />
          <div className="modal__card">
            <h2 className="modal__title">Editar Imagen</h2>
            <p className="modal__sub">Modificar la imagen del combo</p>

            {error && (
              <div className="alert">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleUpdateImage}>
              <div className="field">
                <span>Imagen del Combo</span>
                <ImageUploader
                  onFileSelect={(file) => setSelectedImage(file)}
                  currentImageUrl={selectedCombo.imageUrl}
                  error={error}
                />
              </div>

              <div className="modal__actions">
                <button type="button" className="btn btn--ghost" onClick={closeModal}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn--grad">
                  Guardar
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* VIEW PRODUCTS MODAL */}
      {modalType === "viewProducts" && selectedCombo && (
        <div className="modal">
          <div className="modal__backdrop" onClick={closeModal} />
          <div className="modal__card">
            <h2 className="modal__title">Productos del Combo</h2>
            <p className="modal__sub">{selectedCombo.name}</p>

            <div className="table">
              <div className="table__scroll" style={{ maxHeight: "400px" }}>
                <table>
                  <thead>
                    <tr>
                      <th>Producto</th>
                      <th className="text-center">Cantidad</th>
                      <th className="text-center">Precio Unit.</th>
                      <th className="text-center">Subtotal</th>
                      <th className="text-center">Disponible</th>
                    </tr>
                  </thead>
                  <tbody>
                    {selectedCombo.products.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="table__center muted">
                          No hay productos vinculados
                        </td>
                      </tr>
                    ) : (
                      selectedCombo.products.map((comboProduct, index) => {
                        const fullProduct = products.find((p) => p.id === comboProduct.id);
                        const productName = fullProduct?.name || comboProduct.name || "-";
                        const productPrice = fullProduct?.price || 0;
                        const isAvailable = fullProduct?.available ?? true;
                        const quantity =
                          typeof comboProduct.quantity === "string"
                            ? parseFloat(comboProduct.quantity)
                            : comboProduct.quantity || 0;

                        return (
                          <tr key={index}>
                            <td>{productName}</td>
                            <td className="text-center">{quantity}</td>
                            <td className="text-center">
                              $
                              {(typeof productPrice === "string"
                                ? parseFloat(productPrice)
                                : productPrice || 0
                              ).toFixed(2)}
                            </td>
                            <td className="text-center">
                              $
                              {(
                                (typeof productPrice === "string" ? parseFloat(productPrice) : productPrice || 0) *
                                quantity
                              ).toFixed(2)}
                            </td>
                            <td className="text-center">
                              <span className={isAvailable ? "pill pill--ok" : "pill pill--bad"}>
                                {isAvailable ? "Sí" : "No"}
                              </span>
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            <div style={{ marginTop: "16px", padding: "12px", background: "var(--panel-2)", borderRadius: "8px" }}>
              <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px" }}>
                <span className="muted">Precio Regular Total:</span>
                <span style={{ color: "#fff" }}>
                  $
                  {(typeof selectedCombo.regularPrice === "string"
                    ? parseFloat(selectedCombo.regularPrice)
                    : selectedCombo.regularPrice || 0
                  ).toFixed(2)}
                </span>
              </div>
              {selectedCombo.discount > 0 && (
                <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "8px" }}>
                  <span className="muted">Ahorro:</span>
                  <span style={{ color: "#86efac" }}>
                    -$
                    {(typeof selectedCombo.discount === "number"
                      ? selectedCombo.discount
                      : parseFloat(selectedCombo.discount || "0")
                    ).toFixed(2)}
                  </span>
                </div>
              )}
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  paddingTop: "8px",
                  borderTop: "1px solid var(--border)",
                }}
              >
                <span style={{ color: "var(--blue-2)" }}>Precio Final del Combo:</span>
                <span style={{ color: "var(--blue-2)" }}>
                  $
                  {(typeof selectedCombo.price === "string"
                    ? parseFloat(selectedCombo.price)
                    : selectedCombo.price || 0
                  ).toFixed(2)}
                </span>
              </div>
            </div>

            <div className="modal__actions" style={{ justifyContent: "center" }}>
              <button type="button" className="btn btn--grad" onClick={closeModal}>
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* FILTERS MODAL */}
      {modalType === "filters" && (
        <div className="modal">
          <div className="modal__backdrop" onClick={() => setModalType(null)} />
          <div className="modal__card" style={{ maxWidth: "600px" }}>
            <div
              style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}
            >
              <h2 className="modal__title" style={{ margin: 0 }}>
                Filtros Avanzados
              </h2>
              <button
                onClick={() => setModalType(null)}
                style={{
                  background: "transparent",
                  border: "none",
                  color: "var(--muted)",
                  cursor: "pointer",
                  padding: "4px",
                }}
              >
                <X size={20} />
              </button>
            </div>

            <div className="space">
              {/* Price Range Filter */}
              <div className="field">
                <label>Rango de precio</label>
                <div style={{ display: "flex", gap: "12px", alignItems: "center" }}>
                  <input
                    type="number"
                    placeholder="Mínimo"
                    value={tempMinPrice}
                    onChange={(e) => setTempMinPrice(e.target.value)}
                    step="0.01"
                    min="0"
                    style={{
                      flex: 1,
                      height: "40px",
                      padding: "0 12px",
                      background: "rgba(38,38,38,.3)",
                      border: "1px solid var(--border)",
                      borderRadius: "8px",
                      color: "#fff",
                      fontSize: "14px",
                    }}
                  />
                  <span style={{ color: "var(--muted)" }}>-</span>
                  <input
                    type="number"
                    placeholder="Máximo"
                    value={tempMaxPrice}
                    onChange={(e) => setTempMaxPrice(e.target.value)}
                    step="0.01"
                    min="0"
                    style={{
                      flex: 1,
                      height: "40px",
                      padding: "0 12px",
                      background: "rgba(38,38,38,.3)",
                      border: "1px solid var(--border)",
                      borderRadius: "8px",
                      color: "#fff",
                      fontSize: "14px",
                    }}
                  />
                </div>
              </div>

              {/* Multiple Products Filter */}
              <div className="field">
                <label>Productos incluidos (selecciona varios)</label>
                <p className="field__hint" style={{ marginBottom: "8px" }}>
                  El combo debe contener TODOS los productos seleccionados
                </p>
                <div
                  style={{
                    maxHeight: "300px",
                    overflow: "auto",
                    border: "1px solid var(--border)",
                    borderRadius: "8px",
                    padding: "12px",
                    background: "rgba(38,38,38,.3)",
                  }}
                >
                  {products.length === 0 ? (
                    <p className="muted">No hay productos disponibles</p>
                  ) : (
                    products.map((product) => (
                      <div
                        key={product.id}
                        style={{
                          display: "grid",
                          gridTemplateColumns: "auto 1fr",
                          alignItems: "center",
                          gap: "12px",
                          marginBottom: "10px",
                          padding: "10px 12px",
                          background: tempSelectedProductIds.includes(product.id)
                            ? "rgba(21,93,252,.15)"
                            : "transparent",
                          borderRadius: "6px",
                          border: `1px solid ${tempSelectedProductIds.includes(product.id) ? "rgba(21,93,252,.5)" : "var(--border)"}`,
                          cursor: "pointer",
                        }}
                        onClick={() => toggleProductInFilter(product.id)}
                      >
                        <input
                          type="checkbox"
                          checked={tempSelectedProductIds.includes(product.id)}
                          onChange={() => toggleProductInFilter(product.id)}
                          onClick={(e) => e.stopPropagation()}
                          style={{ cursor: "pointer", width: "16px", height: "16px" }}
                        />
                        <div
                          style={{
                            display: "flex",
                            flexDirection: "column",
                            minWidth: 0,
                          }}
                        >
                          <span
                            style={{
                              color: "#fff",
                              fontSize: "14px",
                              fontWeight: tempSelectedProductIds.includes(product.id) ? "500" : "normal",
                            }}
                          >
                            {product.name}
                          </span>
                          <span
                            style={{
                              color: "var(--muted)",
                              fontSize: "12px",
                            }}
                          >
                            ${product.price.toFixed(2)}
                          </span>
                        </div>
                      </div>
                    ))
                  )}
                </div>
                {tempSelectedProductIds.length > 0 && (
                  <span className="field__hint blue">{tempSelectedProductIds.length} producto(s) seleccionado(s)</span>
                )}
              </div>
            </div>

            <div className="modal__actions">
              <button type="button" className="btn btn--ghost" onClick={clearFilters}>
                Limpiar Filtros
              </button>
              <button type="button" className="btn btn--grad" onClick={applyFilters}>
                Aplicar y Cerrar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* DELETE CONFIRMATION MODAL */}
      {modalType === "delete" && selectedCombo && (
        <div className="modal">
          <div className="modal__backdrop" onClick={closeModal} />
          <div className="modal__card">
            <h2 className="modal__title">Desactivar Combo</h2>
            <p className="modal__sub">¿Estás seguro de que deseas desactivar este combo?</p>

            {error && (
              <div className="alert">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            <div style={{ padding: "12px", background: "var(--panel-2)", borderRadius: "8px", marginBottom: "16px" }}>
              <p style={{ color: "#fff", marginBottom: "4px" }}>{selectedCombo.name}</p>
              <p className="muted" style={{ fontSize: "12px" }}>
                El combo será marcado como inactivo y no estará disponible para los usuarios.
              </p>
            </div>

            <div className="modal__actions">
              <button type="button" className="btn btn--ghost" onClick={closeModal}>
                Cancelar
              </button>
              <button
                type="button"
                className="btn"
                style={{ background: "var(--danger)", color: "#fff" }}
                onClick={handleDeleteCombo}
              >
                Desactivar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* RESTORE CONFIRMATION MODAL */}
      {modalType === "restore" && selectedCombo && (
        <div className="modal">
          <div className="modal__backdrop" onClick={closeModal} />
          <div className="modal__card">
            <h2 className="modal__title">Restaurar Combo</h2>
            <p className="modal__sub">¿Estás seguro de que deseas restaurar este combo?</p>

            {error && (
              <div className="alert">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            <div style={{ padding: "12px", background: "var(--panel-2)", borderRadius: "8px", marginBottom: "16px" }}>
              <p style={{ color: "#fff", marginBottom: "4px" }}>{selectedCombo.name}</p>
              <p className="muted" style={{ fontSize: "12px" }}>
                El combo será marcado como activo y estará disponible para los usuarios.
              </p>
            </div>

            <div className="modal__actions">
              <button type="button" className="btn btn--ghost" onClick={closeModal}>
                Cancelar
              </button>
              <button
                type="button"
                className="btn"
                style={{ background: "var(--danger)", color: "#fff" }}
                onClick={handleRestoreCombo}
              >
                Restaurar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
