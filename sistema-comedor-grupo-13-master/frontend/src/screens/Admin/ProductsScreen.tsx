import {
  AlertCircle,
  ArchiveRestore,
  ArrowLeft,
  DollarSign,
  Edit2,
  Filter,
  Image as ImageIcon,
  Info,
  Loader2,
  Package,
  Pizza,
  Plus,
  Search,
  Trash2,
  X,
  XCircle,
} from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { useLocation } from "wouter";

import { useToken } from "@/services/TokenContext";

import { ImageUploader } from "../../components/Admin/ImageUploader";
import "../../components/Admin/image-uploader.css";
import {
  CreateProductDTO,
  PRODUCT_CATEGORIES,
  Product,
  ProductCategory,
  productAPI,
} from "../../services/ProductServices";
import { Ingredient, ingredientAPI } from "../../services/ingredientAPI";
import "./products-screen.css";

interface ProductsScreenProps {
  onNavigate?: (section: string) => void;
}

interface NewProductForm {
  name: string;
  description: string;
  price: string;
  available: boolean;
  productType: "SIMPLE" | "ELABORATE";
  category: ProductCategory;
  stock: string;
  ingredientIds: number[];
  ingredientQuantities: Record<number, number>;
}

interface FilterState {
  priceMin: string;
  priceMax: string;
  availableFilter: "all" | "true" | "false";
}

function Modal({
  isOpen,
  onClose,
  children,
  className = "",
}: {
  isOpen: boolean;
  onClose: () => void;
  children: React.ReactNode;
  className?: string;
}) {
  if (!isOpen) return null;
  return (
    <div className="modal">
      <div className="modal__backdrop" onClick={onClose} />
      <div className={`modal__card ${className}`}>{children}</div>
    </div>
  );
}

export default function ProductsScreen({ onNavigate }: ProductsScreenProps) {
  const [tokenState] = useToken();
  const [, setLocation] = useLocation();

  const [filters, setFilters] = useState<FilterState>({ priceMin: "", priceMax: "", availableFilter: "all" });
  const [searchTerm, setSearchTerm] = useState("");
  const [products, setProducts] = useState<Product[]>([]);
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [availableIngredients, setAvailableIngredients] = useState<Ingredient[]>([]);

  const [addDialogOpen, setAddDialogOpen] = useState(false);
  const [infoDialog, setInfoDialog] = useState<{ open: boolean; product: Product | null }>({
    open: false,
    product: null,
  });
  const [editNameDialog, setEditNameDialog] = useState<{ open: boolean; product: Product | null }>({
    open: false,
    product: null,
  });
  const [editPriceDialog, setEditPriceDialog] = useState<{ open: boolean; product: Product | null }>({
    open: false,
    product: null,
  });
  const [editStockDialog, setEditStockDialog] = useState<{ open: boolean; product: Product | null }>({
    open: false,
    product: null,
  });
  const [imageDialog, setImageDialog] = useState<{ open: boolean; product: Product | null }>({
    open: false,
    product: null,
  });
  const [restoreDialog, setRestoreDialog] = useState<{ open: boolean; product: Product | null }>({
    open: false,
    product: null,
  });
  const [selectedImage, setSelectedImage] = useState<File | null>(null);
  // const [selectedImagePreview, setSelectedImagePreview] = useState<string | null>(null);
  const [creationImageFile, setCreationImageFile] = useState<File | null>(null);
  const [creationImagePreview, setCreationImagePreview] = useState<string | null>(null);
  const [imageUploading, setImageUploading] = useState(false);
  const [confirmEditPrice, setConfirmEditPrice] = useState(false);
  const [editImagePreview, setEditImagePreview] = useState<string | null>(null);
  const [confirmEditStock, setConfirmEditStock] = useState(false);
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; product: Product | null }>({
    open: false,
    product: null,
  });

  const [formData, setFormData] = useState<NewProductForm>({
    name: "",
    description: "",
    price: "",
    available: true,
    productType: "SIMPLE",
    category: "SANDWICH",
    stock: "",
    ingredientIds: [],
    ingredientQuantities: {},
  });

  const [confirmAdd, setConfirmAdd] = useState(false);
  const [newPrice, setNewPrice] = useState("");
  const [priceError, setPriceError] = useState("");
  const [newName, setNewName] = useState("");
  const [nameError, setNameError] = useState("");
  const [newStock, setNewStock] = useState("");
  const [stockError, setStockError] = useState("");
  const [deleteReason, setDeleteReason] = useState("");
  const [errors, setErrors] = useState<{ name?: string; price?: string; ingredients?: string; stock?: string }>({});
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useEffect(() => {
    const loadIngredients = async () => {
      try {
        const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
        const ingredients = await ingredientAPI.getAll(token);
        setAvailableIngredients(ingredients);
      } catch (err) {
        console.error("Error loading ingredients:", err);
      }
    };
    if (tokenState.state === "LOGGED_IN") loadIngredients();
  }, [tokenState]);

  const showSuccess = (message: string) => {
    setSuccessMessage(message);
    setTimeout(() => setSuccessMessage(null), 3000);
  };

  const loadProducts = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      const data = await productAPI.getAll(token);
      setProducts(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error cargando productos");
      if (err instanceof Error && err.message.includes("No autorizado")) setLocation("/login");
    } finally {
      setLoading(false);
    }
  }, [tokenState, setLocation]);

  useEffect(() => {
    if (tokenState.state === "LOGGED_IN") loadProducts();
  }, [tokenState, loadProducts]);

  const handleNavigate = (section: string) => {
    if (onNavigate) onNavigate(section);
    else if (section === "admin") setLocation("/admin");
  };

  const handleIngredientToggle = (ingredientId: number) => {
    setFormData((prev) => {
      const newIngredientIds = prev.ingredientIds.includes(ingredientId)
        ? prev.ingredientIds.filter((id) => id !== ingredientId)
        : [...prev.ingredientIds, ingredientId];
      const newQuantities = { ...prev.ingredientQuantities };
      if (!newIngredientIds.includes(ingredientId)) delete newQuantities[ingredientId];
      return { ...prev, ingredientIds: newIngredientIds, ingredientQuantities: newQuantities };
    });
  };

  const handleQuantityChange = (ingredientId: number, quantity: number) => {
    setFormData((prev) => ({
      ...prev,
      ingredientQuantities: { ...prev.ingredientQuantities, [ingredientId]: quantity },
    }));
  };

  const handleCreationImageSelect = (file: File) => {
    setCreationImageFile(file);
    const preview = URL.createObjectURL(file);
    setCreationImagePreview(preview);
  };

  const handleProductTypeChange = (productType: "SIMPLE" | "ELABORATE") => {
    setFormData((prev) => ({
      ...prev,
      productType,
      ingredientIds: productType === "SIMPLE" ? [] : prev.ingredientIds,
      ingredientQuantities: productType === "SIMPLE" ? {} : prev.ingredientQuantities,
      stock: productType === "ELABORATE" ? "" : prev.stock,
    }));
    setErrors((prev) => ({ ...prev, ingredients: undefined, stock: undefined }));
  };

  const openRestoreDialog = (product: Product) => {
    if (product.available) return;
    setRestoreDialog({ open: true, product });
  };

  const confirmRestore = async () => {
    if (!restoreDialog.product) return;
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      await productAPI.restore(restoreDialog.product.id, token);
      setRestoreDialog({ open: false, product: null });
      loadProducts();

      showSuccess("Producto reactivado exitosamente");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al restaurar producto");
    }
  };

  const filteredProducts = products.filter((product) => {
    if (searchTerm && !product.name.toLowerCase().includes(searchTerm.toLowerCase())) return false;
    if (filters.priceMin && product.price < parseFloat(filters.priceMin)) return false;
    if (filters.priceMax && product.price > parseFloat(filters.priceMax)) return false;
    if (filters.availableFilter === "true" && !product.available) return false;
    if (filters.availableFilter === "false" && product.available) return false;
    return true;
  });

  const validateForm = () => {
    const newErrors: typeof errors = {};

    if (products.some((prod) => prod.name.toLowerCase() === formData.name.toLowerCase())) {
      newErrors.name = "Ya existe un producto con este nombre";
    }

    const price = parseFloat(formData.price);
    if (!(price > 0)) newErrors.price = "El precio debe ser mayor a 0";

    if (formData.productType === "SIMPLE") {
      const stock = parseFloat(formData.stock);
      if (isNaN(stock) || stock < 0) {
        newErrors.stock = "El stock debe ser un número mayor o igual a 0";
      }
    } else if (formData.productType === "ELABORATE") {
      if (formData.ingredientIds.length === 0) {
        newErrors.ingredients = "Debe seleccionar al menos un ingrediente";
      } else {
        const missing = formData.ingredientIds.filter((id) => {
          const q = formData.ingredientQuantities[id];
          return !(q !== undefined && q !== null && q.toString() !== "" && !isNaN(Number(q)) && Number(q) > 0);
        });
        if (missing.length > 0) {
          const names = missing
            .map((id) => availableIngredients.find((i) => i.id === id)?.name || String(id))
            .join(", ");
          newErrors.ingredients = `Ingrese cantidad mayor a 0 para: ${names}`;
        }
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = () => {
    if (validateForm()) {
      setConfirmAdd(true);
    }
  };

  const confirmAddProduct = async () => {
    setLoading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;

      const dto: CreateProductDTO = {
        name: formData.name,
        description: formData.description,
        price: parseFloat(formData.price),
        available: formData.available,
        productType: formData.productType,
        category: formData.category,
        ingredientIds: formData.ingredientIds,
        ingredientQuantities: Object.fromEntries(
          Object.entries(formData.ingredientQuantities)
            .filter(([, quantity]) => quantity && quantity > 0)
            .map(([id, quantity]) => [id, quantity]),
        ),
      };

      if (formData.productType === "SIMPLE" && formData.stock) {
        dto.stock = parseFloat(formData.stock);
      }

      const createdProduct = await productAPI.create(dto, token);

      // Upload image if one was selected
      if (creationImageFile) {
        try {
          await productAPI.updateImage(createdProduct.id, creationImageFile, token);
        } catch (imageErr) {
          console.warn("Image upload failed, but product was created:", imageErr);
        }
      }

      await loadProducts();

      showSuccess("Producto creado exitosamente");

      setFormData({
        name: "",
        description: "",
        price: "",
        available: true,
        productType: "SIMPLE",
        category: "SANDWICH",
        stock: "",
        ingredientIds: [],
        ingredientQuantities: {},
      });
      setCreationImageFile(null);
      setCreationImagePreview(null);
      setErrors({});
      setConfirmAdd(false);
      setAddDialogOpen(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error creando producto");
      setConfirmAdd(false); // Close confirmation dialog on error
    } finally {
      setLoading(false);
    }
  };

  const handleEditPrice = () => {
    if (!editPriceDialog.product || !newPrice || parseFloat(newPrice) <= 0) {
      setPriceError("El precio debe ser mayor a 0");
      return;
    }
    setPriceError("");
    setConfirmEditPrice(true);
  };

  const confirmEditPriceAction = async () => {
    if (!editPriceDialog.product || !newPrice) return;
    setLoading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      await productAPI.updatePrice(editPriceDialog.product.id, parseFloat(newPrice), token);
      await loadProducts();

      showSuccess("Precio modificado exitosamente");

      setEditPriceDialog({ open: false, product: null });
      setNewPrice("");
      setPriceError("");
      setConfirmEditPrice(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error modificando precio");
      setConfirmEditPrice(false);
    } finally {
      setLoading(false);
    }
  };

  const handleEditStock = () => {
    if (!editStockDialog.product || !newStock || parseFloat(newStock) < 0) {
      setStockError("El stock debe ser mayor o igual a 0");
      return;
    }
    setStockError("");
    setConfirmEditStock(true);
  };

  const confirmEditStockAction = async () => {
    if (!editStockDialog.product || !newStock) return;
    setLoading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;

      // Llamar al endpoint específico de stock
      await productAPI.updateStock(editStockDialog.product.id, parseFloat(newStock), token);

      await loadProducts();

      showSuccess("Stock modificado exitosamente");

      setEditStockDialog({ open: false, product: null });
      setNewStock("");
      setStockError("");
      setConfirmEditStock(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error modificando stock");
      setConfirmEditStock(false);
    } finally {
      setLoading(false);
    }
  };

  const handleEditImageSelect = (file: File) => {
    setSelectedImage(file);
    const preview = URL.createObjectURL(file);
    setEditImagePreview(preview);
  };

  const confirmImageUpload = async () => {
    if (!imageDialog.product || !selectedImage) return;
    setImageUploading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      await productAPI.updateImage(imageDialog.product.id, selectedImage, token);
      await loadProducts();

      showSuccess("Imagen modificada exitosamente");

      setImageDialog({ open: false, product: null });
      setSelectedImage(null);
      setEditImagePreview(null);

      if (editImagePreview) {
        URL.revokeObjectURL(editImagePreview);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error cargando imagen");
    } finally {
      setImageUploading(false);
    }
  };

  const handleDeactivate = (product: Product) => {
    setDeleteDialog({ open: true, product });
    setDeleteReason("");
  };

  const confirmDeactivate = async () => {
    if (!deleteDialog.product) return;
    setLoading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      await productAPI.deactivate(deleteDialog.product.id, deleteReason || undefined, token);
      await loadProducts();

      showSuccess("Producto desactivado exitosamente");

      setDeleteDialog({ open: false, product: null });
      setDeleteReason("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error desactivando producto");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="container">
        {/* Header */}
        <div className="header">
          <div className="badge">
            <Pizza size={28} className="badge__ico" />
          </div>
          <p className="header__title">Gestión de Productos</p>
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
              <p className="card__title">Productos</p>
              <button className="btn btn--grad" onClick={() => setAddDialogOpen(true)} disabled={loading}>
                <Plus size={16} /> <span>Agregar Producto</span>
              </button>
            </div>

            {/* Search + Filters */}
            <div className="filters">
              <div className="input input--with-ico">
                <Search size={16} className="input__ico" />
                <input
                  type="text"
                  placeholder="Buscar productos…"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
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
                      <label>Rango de Precio</label>
                      <div className="row">
                        <input
                          type="number"
                          placeholder="Mínimo"
                          value={filters.priceMin}
                          onChange={(e) => setFilters((p) => ({ ...p, priceMin: e.target.value }))}
                        />
                        <span className="dash">-</span>
                        <input
                          type="number"
                          placeholder="Máximo"
                          value={filters.priceMax}
                          onChange={(e) => setFilters((p) => ({ ...p, priceMax: e.target.value }))}
                        />
                      </div>
                    </div>

                    <div className="field">
                      <label>Estado</label>
                      <div className="row">
                        {(["all", "true", "false"] as const).map((v) => (
                          <button
                            key={v}
                            className={`chip ${filters.availableFilter === v ? "chip--active" : ""}`}
                            onClick={() => setFilters((p) => ({ ...p, availableFilter: v }))}
                          >
                            {v === "all" ? "Todos" : v === "true" ? "Disponible" : "No Disponible"}
                          </button>
                        ))}
                      </div>
                    </div>

                    <button
                      className="btn btn--ghost w-full"
                      onClick={() => setFilters({ priceMin: "", priceMax: "", availableFilter: "all" })}
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
                      <th className="hide-md">Descripción</th>
                      <th className="hide-md">Precio</th>
                      <th className="hide-md">Tipo</th>
                      <th className="hide-md">Stock</th>
                      <th className="hide-md text-center">Estado</th>
                      <th className="text-right">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {loading ? (
                      <tr>
                        <td colSpan={7} className="table__center muted">
                          <Loader2 size={16} className="spin" /> Cargando productos…
                        </td>
                      </tr>
                    ) : filteredProducts.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="table__center muted">
                          No se encontraron productos con los filtros aplicados
                        </td>
                      </tr>
                    ) : (
                      filteredProducts.map((product) => (
                        <tr key={product.id}>
                          <td>{product.name}</td>
                          <td className="muted hide-md">{product.description}</td>
                          <td className="muted hide-md">${product.price}</td>
                          <td className="muted hide-md">{product.productType === "SIMPLE" ? "Simple" : "Elaborado"}</td>
                          <td className="muted hide-md">
                            {product.productType === "SIMPLE"
                              ? product.stock !== undefined
                                ? product.stock
                                : "0"
                              : product.stock !== undefined && product.stock > 0
                                ? product.stock
                                : "0"}
                          </td>
                          <td className="muted hide-md text-center">
                            {!product.active ? (
                              <span className="pill pill--bad">Inactivo</span>
                            ) : product.available ? (
                              <span className="pill pill--ok">Disponible</span>
                            ) : (
                              <span className="pill pill--warn">Sin Stock</span>
                            )}
                          </td>
                          <td className="text-right">
                            <div className="row-end">
                              <button
                                className="icon-btn info md-hide"
                                onClick={() => setInfoDialog({ open: true, product })}
                                title="Ver información"
                              >
                                <Info size={14} />
                              </button>
                              <button
                                className="icon-btn info"
                                onClick={() => {
                                  setEditNameDialog({ open: true, product });
                                  setNewName(product.name);
                                }}
                                title="Modificar nombre"
                              >
                                <Edit2 size={14} />
                              </button>
                              <button
                                className="icon-btn info"
                                onClick={() => {
                                  setEditPriceDialog({ open: true, product });
                                  setNewPrice(product.price.toString());
                                  setPriceError("");
                                }}
                                title="Modificar Precio"
                              >
                                <DollarSign size={14} />
                              </button>
                              <button
                                className={`icon-btn ${product.productType === "SIMPLE" ? "info" : "disabled"}`}
                                onClick={() => {
                                  if (product.productType === "SIMPLE") {
                                    setEditStockDialog({ open: true, product });
                                    setNewStock(product.stock?.toString() || "0");
                                    setStockError("");
                                  }
                                }}
                                title={
                                  product.productType === "SIMPLE" ? "Modificar Stock" : "Solo para productos simples"
                                }
                                disabled={product.productType !== "SIMPLE"}
                              >
                                <Package size={14} />
                              </button>
                              <button
                                className="icon-btn info"
                                onClick={() => setImageDialog({ open: true, product })}
                                title="Editar imagen"
                              >
                                <ImageIcon size={14} />
                              </button>
                              {product.active ? (
                                <button
                                  className="icon-btn danger"
                                  onClick={() => handleDeactivate(product)}
                                  title="Desactivar"
                                  disabled={!product.available}
                                >
                                  <Trash2 size={14} />
                                </button>
                              ) : (
                                <button
                                  className="icon-btn success"
                                  onClick={() => openRestoreDialog(product)}
                                  title="Restaurar"
                                >
                                  <ArchiveRestore size={14} />
                                </button>
                              )}
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
      <Modal isOpen={infoDialog.open} onClose={() => setInfoDialog({ open: false, product: null })}>
        <h2 className="modal__title">Información del Producto</h2>
        {infoDialog.product && (
          <div className="space">
            <div>
              <label>Nombre</label>
              <p>{infoDialog.product.name}</p>
            </div>
            <div>
              <label>Descripción</label>
              <p>{infoDialog.product.description}</p>
            </div>
            <div className="grid2">
              <div>
                <label>Precio</label>
                <p>${infoDialog.product?.price}</p>
              </div>
              <div>
                <label>Estado</label>
                <p>
                  {!infoDialog.product?.active ? (
                    <span className="pill pill--bad">Inactivo</span>
                  ) : infoDialog.product?.available ? (
                    <span className="pill pill--ok">Disponible</span>
                  ) : (
                    <span className="pill pill--warn">Sin Stock</span>
                  )}
                </p>
              </div>
            </div>
            <div className="grid2">
              <div>
                <label>Tipo</label>
                <p>{infoDialog.product.productType === "SIMPLE" ? "Simple" : "Elaborado"}</p>
              </div>
              {infoDialog.product.productType === "SIMPLE" && infoDialog.product.stock !== undefined && (
                <div>
                  <label>Stock</label>
                  <p>{infoDialog.product.stock}</p>
                </div>
              )}
            </div>
            <button
              className="btn btn--secondary w-full"
              onClick={() => setImageDialog({ open: true, product: infoDialog.product })}
            >
              Cambiar Imagen
            </button>
            <button className="btn btn--grad w-full" onClick={() => setInfoDialog({ open: false, product: null })}>
              Cerrar
            </button>
          </div>
        )}
      </Modal>

      {/* Edit name */}
      <Modal
        isOpen={editNameDialog.open}
        onClose={() => {
          setEditNameDialog({ open: false, product: null });
          setNewName("");
          setNameError("");
        }}
      >
        <h2 className="modal__title">Modificar Nombre</h2>
        <p className="modal__sub">Modificando: {editNameDialog.product?.name}</p>
        <label className="field">
          <span>Nuevo Nombre</span>
          <input
            value={newName}
            onChange={(e) => {
              setNewName(e.target.value);
              setNameError("");
            }}
            placeholder="Ingrese nuevo nombre"
          />
          {nameError && <small className="field__error">{nameError}</small>}
        </label>
        <div className="modal__actions">
          <button
            className="btn btn--ghost"
            onClick={() => {
              setEditNameDialog({ open: false, product: null });
              setNewName("");
              setNameError("");
            }}
          >
            Cancelar
          </button>
          <button
            className="btn btn--grad"
            onClick={async () => {
              if (!newName.trim()) {
                setNameError("El nombre no puede estar vacío");
                return;
              }
              if (
                products.some(
                  (p) => p.id !== editNameDialog.product?.id && p.name.toLowerCase() === newName.toLowerCase(),
                )
              ) {
                setNameError("Ya existe un producto con este nombre");
                return;
              }
              if (!editNameDialog.product) return;
              setLoading(true);
              try {
                const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
                await productAPI.updateName(editNameDialog.product.id, newName, token);
                await loadProducts();

                showSuccess("Nombre modificado exitosamente");

                setEditNameDialog({ open: false, product: null });
                setNewName("");
                setNameError("");
              } catch (err) {
                setError(err instanceof Error ? err.message : "Error modificando nombre");
              } finally {
                setLoading(false);
              }
            }}
          >
            {loading ? <Loader2 size={16} className="spin" /> : null} Guardar
          </button>
        </div>
      </Modal>

      {/* Edit price */}
      <Modal
        isOpen={editPriceDialog.open && !confirmEditPrice}
        onClose={() => {
          setEditPriceDialog({ open: false, product: null });
          setPriceError("");
        }}
      >
        <h2 className="modal__title">Modificar Precio</h2>
        <p className="modal__sub">Modificando: {editPriceDialog.product?.name}</p>
        <label className="field">
          <span>Nuevo Precio</span>
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={newPrice}
            onChange={(e) => {
              setNewPrice(e.target.value);
              setPriceError("");
            }}
            placeholder="0.00"
          />
          {priceError && <small className="field__error">{priceError}</small>}
          <small className="field__hint">Precio actual: ${editPriceDialog.product?.price}</small>
        </label>
        <div className="modal__actions">
          <button
            className="btn btn--ghost"
            onClick={() => {
              setEditPriceDialog({ open: false, product: null });
              setPriceError("");
            }}
          >
            Cancelar
          </button>
          <button
            className="btn btn--grad"
            onClick={handleEditPrice}
            disabled={!newPrice || parseFloat(newPrice) <= 0 || !!priceError}
          >
            Modificar
          </button>
        </div>
      </Modal>

      {/* Confirm edit price */}
      <Modal isOpen={confirmEditPrice} onClose={() => setConfirmEditPrice(false)}>
        <h2 className="modal__title">Confirmar Operación</h2>
        <p className="modal__sub">
          El precio pasará de ${editPriceDialog.product?.price} a ${newPrice}.
        </p>
        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setConfirmEditPrice(false)}>
            Cancelar
          </button>
          <button className="btn btn--grad" onClick={confirmEditPriceAction}>
            {loading ? <Loader2 size={16} className="spin" /> : null} Confirmar
          </button>
        </div>
      </Modal>

      {/* Edit stock */}
      <Modal
        isOpen={editStockDialog.open && !confirmEditStock}
        onClose={() => {
          setEditStockDialog({ open: false, product: null });
          setStockError("");
        }}
      >
        <h2 className="modal__title">Modificar Stock</h2>
        <p className="modal__sub">Modificando: {editStockDialog.product?.name}</p>
        <label className="field">
          <span>Nuevo Stock</span>
          <input
            type="number"
            min="0"
            step="1"
            value={newStock}
            onChange={(e) => {
              setNewStock(e.target.value);
              setStockError("");
            }}
            placeholder="0"
          />
          {stockError && <small className="field__error">{stockError}</small>}
          <small className="field__hint">Stock actual: {editStockDialog.product?.stock}</small>
        </label>
        <div className="modal__actions">
          <button
            className="btn btn--ghost"
            onClick={() => {
              setEditStockDialog({ open: false, product: null });
              setStockError("");
            }}
          >
            Cancelar
          </button>
          <button
            className="btn btn--grad"
            onClick={handleEditStock}
            disabled={!newStock || parseFloat(newStock) < 0 || !!stockError}
          >
            Modificar
          </button>
        </div>
      </Modal>

      {/* Confirm edit stock */}
      <Modal isOpen={confirmEditStock} onClose={() => setConfirmEditStock(false)}>
        <h2 className="modal__title">Confirmar Operación</h2>
        <p className="modal__sub">
          El stock pasará de {editStockDialog.product?.stock} a {newStock}.
        </p>
        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setConfirmEditStock(false)}>
            Cancelar
          </button>
          <button className="btn btn--grad" onClick={confirmEditStockAction}>
            {loading ? <Loader2 size={16} className="spin" /> : null} Confirmar
          </button>
        </div>
      </Modal>

      {/* Delete / Deactivate */}
      <Modal isOpen={deleteDialog.open} onClose={() => setDeleteDialog({ open: false, product: null })}>
        <h2 className="modal__title">Confirmar Desactivación</h2>
        <p className="modal__sub">El producto quedará marcado como no disponible.</p>

        <label className="field">
          <span>Razón (opcional)</span>
          <textarea
            rows={3}
            value={deleteReason}
            onChange={(e) => setDeleteReason(e.target.value)}
            placeholder="Ingrese la razón…"
          />
        </label>

        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setDeleteDialog({ open: false, product: null })}>
            Cancelar
          </button>
          <button className="btn btn--danger" onClick={confirmDeactivate}>
            {loading ? <Loader2 size={16} className="spin" /> : null}
            Desactivar
          </button>
        </div>
      </Modal>

      {/* Restore */}
      <Modal isOpen={restoreDialog.open} onClose={() => setRestoreDialog({ open: false, product: null })}>
        <h2 className="modal__title">Restaurar Producto</h2>
        <p className="modal__sub">¿Deseas reactivar "{restoreDialog.product?.name}"?</p>

        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setRestoreDialog({ open: false, product: null })}>
            Cancelar
          </button>
          <button className="btn btn--grad" onClick={confirmRestore} disabled={loading}>
            {loading ? <Loader2 size={16} className="spin" /> : null}
            Restaurar
          </button>
        </div>
      </Modal>

      {/* Image Upload */}
      <Modal
        isOpen={imageDialog.open}
        onClose={() => {
          setImageDialog({ open: false, product: null });
          setSelectedImage(null);
          setEditImagePreview(null);
          if (editImagePreview) {
            URL.revokeObjectURL(editImagePreview);
          }
        }}
      >
        <h2 className="modal__title">Cambiar Imagen</h2>
        <p className="modal__sub">Producto: {imageDialog.product?.name}</p>
        <div className="field">
          <span>Imagen del Producto</span>
          <ImageUploader
            onFileSelect={handleEditImageSelect}
            currentImageUrl={editImagePreview || imageDialog.product?.image}
            error={error}
          />
        </div>
        <div className="modal__actions">
          <button
            className="btn btn--ghost"
            onClick={() => {
              setImageDialog({ open: false, product: null });
              setSelectedImage(null);
              setEditImagePreview(null);
              if (editImagePreview) {
                URL.revokeObjectURL(editImagePreview);
              }
            }}
          >
            Cancelar
          </button>
          <button className="btn btn--grad" onClick={confirmImageUpload} disabled={!selectedImage || imageUploading}>
            {imageUploading ? <Loader2 size={16} className="spin" /> : null}
            Cargar Imagen
          </button>
        </div>
      </Modal>

      {/* Create product */}
      <Modal isOpen={addDialogOpen && !confirmAdd} onClose={() => setAddDialogOpen(false)} className="modal--md">
        <h2 className="modal__title">Agregar Nuevo Producto</h2>
        <p className="modal__sub">Complete todos los campos requeridos</p>
        <div className="space">
          <label className="field">
            <span>Nombre *</span>
            <input
              value={formData.name}
              onChange={(e) => {
                setFormData((p) => ({ ...p, name: e.target.value }));
                setErrors((p) => ({ ...p, name: undefined }));
              }}
              placeholder="Ej: Pizza Margherita"
            />
            {errors.name && <small className="field__error">{errors.name}</small>}
          </label>

          <label className="field">
            <span>Descripción *</span>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData((p) => ({ ...p, description: e.target.value }))}
              placeholder="Ej: Pizza clásica con tomate y mozzarella"
              rows={3}
            />
          </label>

          <div className="field">
            <span>Imagen del Producto</span>
            <ImageUploader onFileSelect={handleCreationImageSelect} previewUrl={creationImagePreview} error={error} />
          </div>

          <label className="field">
            <span>Precio *</span>
            <input
              type="number"
              min="0.01"
              step="0.01"
              value={formData.price}
              onChange={(e) => {
                setFormData((p) => ({ ...p, price: e.target.value }));
                setErrors((p) => ({ ...p, price: undefined }));
              }}
              placeholder="0.00"
            />
            {errors.price && <small className="field__error">{errors.price}</small>}
          </label>

          <label className="field">
            <span>Tipo de Producto *</span>
            <select
              value={formData.productType}
              onChange={(e) => handleProductTypeChange(e.target.value as "SIMPLE" | "ELABORATE")}
            >
              <option value="ELABORATE">Producto Elaborado</option>
              <option value="SIMPLE">Producto Simple</option>
            </select>
          </label>

          <label className="field">
            <span>Categoría *</span>
            <select
              value={formData.category}
              onChange={(e) => setFormData((p) => ({ ...p, category: e.target.value as ProductCategory }))}
            >
              {Object.entries(PRODUCT_CATEGORIES).map(([key, label]) => (
                <option key={key} value={key}>
                  {label}
                </option>
              ))}
            </select>
          </label>

          {formData.productType === "SIMPLE" && (
            <label className="field">
              <span>Stock *</span>
              <input
                type="number"
                min="0"
                step="1"
                value={formData.stock}
                onChange={(e) => {
                  setFormData((p) => ({ ...p, stock: e.target.value }));
                  setErrors((p) => ({ ...p, stock: undefined }));
                }}
                placeholder="Cantidad en stock"
              />
              {errors.stock && <small className="field__error">{errors.stock}</small>}
            </label>
          )}

          <label className="field field--row">
            <input
              type="checkbox"
              checked={formData.available}
              onChange={(e) => setFormData((p) => ({ ...p, available: e.target.checked }))}
            />
            <span>Disponible para venta</span>
          </label>
        </div>

        {formData.productType === "ELABORATE" && (
          <div className="field">
            <span>Ingredientes *</span>
            {errors.ingredients && <small className="field__error">{errors.ingredients}</small>}
            <div className="ingredients">
              {availableIngredients.map((ingredient) => (
                <div key={ingredient.id} className="ingredient-item">
                  <input
                    type="checkbox"
                    checked={formData.ingredientIds.includes(ingredient.id)}
                    onChange={() => handleIngredientToggle(ingredient.id)}
                  />
                  <span className="ingredient-item__name">{ingredient.name}</span>
                  {formData.ingredientIds.includes(ingredient.id) && (
                    <div className="ingredient-item__qty">
                      <input
                        type="number"
                        min="0.01"
                        step="0.01"
                        value={formData.ingredientQuantities[ingredient.id] || ""}
                        onChange={(e) => handleQuantityChange(ingredient.id, Number(e.target.value))}
                        placeholder="Cantidad"
                      />
                      <span className="ingredient-item__um">{ingredient.unitMeasure}</span>
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setAddDialogOpen(false)} disabled={loading}>
            Cancelar
          </button>
          <button
            className="btn btn--grad"
            onClick={handleSubmit}
            disabled={!formData.name || !formData.description || !formData.price || loading}
          >
            Crear Producto
          </button>
        </div>
      </Modal>

      {/* Confirm create */}
      <Modal isOpen={confirmAdd} onClose={() => setConfirmAdd(false)}>
        <h2 className="modal__title">Confirmar Nuevo Producto</h2>
        <p className="modal__sub">
          ¿Crear "{formData.name}" ({formData.productType === "SIMPLE" ? "Simple" : "Elaborado"}) con precio $
          {formData.price}?
          {formData.productType === "SIMPLE" && formData.stock && ` y stock de ${formData.stock} unidades`}
        </p>
        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setConfirmAdd(false)} disabled={loading}>
            Cancelar
          </button>
          <button className="btn btn--grad" onClick={confirmAddProduct} disabled={loading}>
            {loading ? <Loader2 size={16} className="spin" /> : null} Confirmar
          </button>
        </div>
      </Modal>
    </div>
  );
}
