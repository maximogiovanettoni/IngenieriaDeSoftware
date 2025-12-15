import { useState, useEffect, useCallback } from "react";
import { Plus, Trash2, Search, XCircle, Users, AlertCircle, Loader2, Mail, Clock, ArrowLeft } from "lucide-react";
import { useToken } from "@/services/TokenContext";
import { useLocation } from "wouter";
import { staffAPI } from "@/services/staffAPI";
import "./staff-management.css";

interface Staff {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  createdAt: string;
  role: string;
}

interface CreateStaffForm {
  firstName: string;
  lastName: string;
  email: string;
  temporaryPassword: string;
  birthDate?: string;
  gender?: string;
}

interface FilterState {
  searchEmail: string;
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

export default function StaffManagementScreen() {
  const [tokenState] = useToken();
  const [, setLocation] = useLocation();

  const [filters, setFilters] = useState<FilterState>({ searchEmail: "" });
  const [staff, setStaff] = useState<Staff[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [addDialogOpen, setAddDialogOpen] = useState(false);
  const [deleteDialog, setDeleteDialog] = useState<{ open: boolean; staff: Staff | null }>({ open: false, staff: null });
  const [confirmAdd, setConfirmAdd] = useState(false);
  const [deleteReason, setDeleteReason] = useState("");
  const [errors, setErrors] = useState<{ firstName?: string; lastName?: string; email?: string }>({});

  const [formData, setFormData] = useState<CreateStaffForm>({
    firstName: "",
    lastName: "",
    email: "",
    temporaryPassword: "",
    birthDate: "",
    gender: "male",
  });

  useEffect(() => {
    if (tokenState.state !== "LOGGED_IN") {
      setLocation("/login");
      return;
    }
  }, [tokenState, setLocation]);

  const loadStaff = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      if (tokenState.state !== "LOGGED_IN") {
        setLocation("/login");
        return;
      }
      const token = tokenState.tokens.accessToken;
      const data = await staffAPI.getAll(token);
      setStaff(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error cargando staff");
      console.error("Error loading staff:", err);
    } finally {
      setLoading(false);
    }
  }, [tokenState, setLocation]);

  useEffect(() => {
    if (tokenState.state === "LOGGED_IN") loadStaff();
  }, [tokenState, loadStaff]);

  const filteredStaff = staff.filter((s) =>
    filters.searchEmail ? s.email.toLowerCase().includes(filters.searchEmail.toLowerCase()) : true
  );

  const validateForm = () => {
    const newErrors: typeof errors = {};
    if (!formData.firstName.trim()) newErrors.firstName = "El nombre es requerido";
    if (!formData.lastName.trim()) newErrors.lastName = "El apellido es requerido";
    if (!formData.email.trim()) newErrors.email = "El email es requerido";
    if (staff.some((s) => s.email.toLowerCase() === formData.email.toLowerCase()))
      newErrors.email = "Ya existe un staff con este email";
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = () => {
    if (validateForm()) setConfirmAdd(true);
  };

  const confirmAddStaff = async () => {
    if (tokenState.state !== "LOGGED_IN") {
      setError("No estás autenticado");
      return;
    }
    setLoading(true);
    try {
      const token = tokenState.tokens.accessToken;
      await staffAPI.create(
        {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          temporaryPassword: formData.temporaryPassword,
          birthDate: formData.birthDate,
          gender: formData.gender,
        },
        token
      );
      await loadStaff();
      setFormData({ firstName: "", lastName: "", email: "", temporaryPassword: "", birthDate: "", gender: "male" });
      setErrors({});
      setConfirmAdd(false);
      setAddDialogOpen(false);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error creando staff");
      console.error("Error creating staff:", err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = (staffMember: Staff) => {
    setDeleteDialog({ open: true, staff: staffMember });
    setDeleteReason("");
  };

  const confirmDelete = async () => {
    if (!deleteDialog.staff) return;
    setLoading(true);
    try {
      const token = tokenState.state === "LOGGED_IN" ? tokenState.tokens.accessToken : undefined;
      await staffAPI.delete(deleteDialog.staff.email, deleteReason || undefined, token);
      await loadStaff();
      setDeleteDialog({ open: false, staff: null });
      setDeleteReason("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error eliminando staff");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="center">
        <header className="header">
          <div className="logo-badge">
            <Users className="logo-badge__ico" size={32} />
          </div>
          <p className="header__subtitle">Gestión de Staff</p>
        </header>

        {error && (
          <div className="alert alert--error">
            <AlertCircle size={18} />
            <span>{error}</span>
            <button className="alert__close" onClick={() => setError(null)}>
              <XCircle size={18} />
            </button>
          </div>
        )}

        <div className="card card--glow">
          <div className="card__body">
            <div className="card__head">
              <p className="card__title">Personal del Comedor</p>
              <button className="btn btn--grad" onClick={() => setAddDialogOpen(true)} disabled={loading}>
                <Plus size={16} />
                <span>Agregar Staff</span>
              </button>
            </div>

            <div className="filters">
              <div className="input input--with-ico">
                <Search size={16} className="input__ico" />
                <input
                  type="text"
                  placeholder="Buscar por email…"
                  value={filters.searchEmail}
                  onChange={(e) => setFilters({ searchEmail: e.target.value })}
                />
              </div>
              <button className="btn btn--outline" onClick={() => setFilters({ searchEmail: "" })}>
                <XCircle size={16} />
                <span>Limpiar</span>
              </button>
            </div>

            <div className="table">
              <div className="table__scroll">
                <table>
                  <thead>
                    <tr>
                      <th>Nombre</th>
                      <th className="hide-md">Email</th>
                      <th className="hide-md">Fecha de Creación</th>
                      <th className="text-right">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {loading ? (
                      <tr>
                        <td colSpan={4} className="table__center muted">
                          <Loader2 size={16} className="spin" /> Cargando staff…
                        </td>
                      </tr>
                    ) : filteredStaff.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="table__center muted">
                          No se encontró personal
                        </td>
                      </tr>
                    ) : (
                      filteredStaff.map((s) => (
                        <tr key={s.id}>
                          <td>{s.firstName} {s.lastName}</td>
                          <td className="muted hide-md">
                            <Mail size={12} /> {s.email}
                          </td>
                          <td className="muted hide-md">
                            <Clock size={12} /> {new Date(s.createdAt).toLocaleDateString("es-AR")}
                          </td>
                          <td className="text-right">
                            <button className="icon-btn icon-btn--danger" onClick={() => handleDelete(s)} title="Eliminar">
                              <Trash2 size={14} />
                            </button>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            <button className="back" onClick={() => setLocation("/admin")}>
              <ArrowLeft size={14} />
              Volver a Menú Inicial
            </button>
          </div>
        </div>
      </div>

      <Modal isOpen={addDialogOpen && !confirmAdd} onClose={() => setAddDialogOpen(false)} className="modal--md">
        <h2 className="modal__title">Agregar Nuevo Staff</h2>
        <p className="modal__sub">Complete todos los campos requeridos</p>

        <div className="form-grid">
          <label className="field">
            <span>Nombre *</span>
            <input
              value={formData.firstName}
              onChange={(e) => {
                setFormData((p) => ({ ...p, firstName: e.target.value }));
                setErrors((p) => ({ ...p, firstName: undefined }));
              }}
              placeholder="Ej: Juan"
            />
            {errors.firstName && <small className="field__error">{errors.firstName}</small>}
          </label>

          <label className="field">
            <span>Apellido *</span>
            <input
              value={formData.lastName}
              onChange={(e) => {
                setFormData((p) => ({ ...p, lastName: e.target.value }));
                setErrors((p) => ({ ...p, lastName: undefined }));
              }}
              placeholder="Ej: Pérez"
            />
            {errors.lastName && <small className="field__error">{errors.lastName}</small>}
          </label>

          <label className="field">
            <span>Email Institucional *</span>
            <input
              value={formData.email}
              onChange={(e) => {
                setFormData((p) => ({ ...p, email: e.target.value }));
                setErrors((p) => ({ ...p, email: undefined }));
              }}
              placeholder="Ej: juan.perez@fi.uba.ar"
            />
            {errors.email && <small className="field__error">{errors.email}</small>}
          </label>

          <label className="field">
            <span>Contraseña Temporal *</span>
            <input
              value={formData.temporaryPassword}
              onChange={(e) => setFormData((p) => ({ ...p, temporaryPassword: e.target.value }))}
              type="password"
              placeholder="Ej: Staffcomedor999"
            />
            <small className="field__hint">Mínimo 8 caracteres</small>
          </label>

          <label className="field">
            <span>Fecha de Nacimiento *</span>
            <input
              type="date"
              value={formData.birthDate}
              onChange={(e) => setFormData((p) => ({ ...p, birthDate: e.target.value }))}
            />
          </label>

          <label className="field">
            <span>Género *</span>
            <select
              value={formData.gender}
              onChange={(e) => setFormData((p) => ({ ...p, gender: e.target.value }))}
            >
              <option value="male">Masculino</option>
              <option value="female">Femenino</option>
              <option value="other">Otro</option>
            </select>
          </label>
        </div>

        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setAddDialogOpen(false)} disabled={loading}>
            Cancelar
          </button>
          <button
            className="btn btn--grad"
            onClick={handleSubmit}
            disabled={!formData.firstName || !formData.lastName || !formData.email || !formData.temporaryPassword || !formData.birthDate || loading}
          >
            Crear Staff
          </button>
        </div>
      </Modal>

      {/* Confirm Add */}
      <Modal isOpen={confirmAdd} onClose={() => setConfirmAdd(false)}>
        <h2 className="modal__title">Confirmar Nuevo Staff</h2>
        <p className="modal__sub">
          ¿Está seguro que desea crear el staff "{formData.firstName} {formData.lastName}" con email "{formData.email}"?
        </p>
        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setConfirmAdd(false)} disabled={loading}>
            Cancelar
          </button>
          <button className="btn btn--grad" onClick={confirmAddStaff} disabled={loading}>
            {loading ? <Loader2 size={16} className="spin" /> : null} Confirmar
          </button>
        </div>
      </Modal>

      {/* Delete */}
      <Modal isOpen={deleteDialog.open} onClose={() => setDeleteDialog({ open: false, staff: null })}>
        <h2 className="modal__title">Confirmar Eliminación</h2>
        <p className="modal__sub">
          ¿Está seguro que desea eliminar el staff "{deleteDialog.staff?.firstName} {deleteDialog.staff?.lastName}" ({deleteDialog.staff?.email})?
        </p>
        <label className="field">
          <span>Razón (opcional)</span>
          <textarea
            value={deleteReason}
            onChange={(e) => setDeleteReason(e.target.value)}
            placeholder="Ingrese la razón de eliminación…"
            maxLength={200}
            rows={3}
          />
        </label>
        <div className="modal__actions">
          <button className="btn btn--ghost" onClick={() => setDeleteDialog({ open: false, staff: null })} disabled={loading}>
            Cancelar
          </button>
          <button className="btn btn--danger" onClick={confirmDelete} disabled={loading}>
            {loading ? <Loader2 size={16} className="spin" /> : null} Eliminar
          </button>
        </div>
      </Modal>
    </div>
  );
}
