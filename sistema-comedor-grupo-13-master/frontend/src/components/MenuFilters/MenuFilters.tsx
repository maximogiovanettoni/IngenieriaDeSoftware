import { Search, X } from 'lucide-react';
import { useState, useEffect, useCallback } from 'react';
import './menu-filters.css';

export interface MenuFiltersState {
  category: string;
  searchQuery: string;
}

interface MenuFiltersProps {
  onFiltersChange: (filters: MenuFiltersState) => void;
  itemsFound?: number;
}

const CATEGORIES = [
  { id: 'all',       label: 'Todos' },
  { id: 'SANDWICH',  label: 'Sándwich' },
  { id: 'DRINK',     label: 'Bebida' },
  { id: 'DESSERT',   label: 'Postre' },
  { id: 'SALAD',     label: 'Ensalada' },
  { id: 'MAIN_COURSE', label: 'Plato Principal' },
  { id: 'COMBO',     label: 'Combo' },
  { id: 'SIDE_DISH', label: 'Acompañamiento' },
  { id: 'COFFEE',    label: 'Café' },
];

export const MenuFilters: React.FC<MenuFiltersProps> = ({ onFiltersChange, itemsFound = 0 }) => {
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');

  // Debounce de 300ms
  useEffect(() => {
    const t = setTimeout(() => setDebouncedQuery(searchQuery), 300);
    return () => clearTimeout(t);
  }, [searchQuery]);

  // Notificar cambios
  useEffect(() => {
    onFiltersChange({ category: selectedCategory, searchQuery: debouncedQuery });
  }, [selectedCategory, debouncedQuery, onFiltersChange]);

  const handleClearSearch = useCallback(() => setSearchQuery(''), []);

  return (
    <div className="mf">
      {/* Categorías */}
      <div className="mf__block">
        <p className="mf__section-title">Categorías</p>
        <div className="mf__chips">
          {CATEGORIES.map((cat) => (
            <button
              key={cat.id}
              className={`mf-chip ${selectedCategory === cat.id ? 'is-active' : ''}`}
              onClick={() => setSelectedCategory(cat.id)}
            >
              {cat.label}
            </button>
          ))}
        </div>
      </div>

      {/* Búsqueda */}
      <div className="mf__block">
        <div className="mf__search">
          <Search size={18} className="mf__icon mf__icon--left" aria-hidden />
          <input
            className="mf-input"
            type="text"
            placeholder="Buscar por nombre…"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
          {searchQuery && (
            <button className="mf__clear" onClick={handleClearSearch} aria-label="Limpiar búsqueda">
              <X size={18} />
            </button>
          )}
        </div>
      </div>

      {/* Resultado */}
      {itemsFound > 0 && (
        <p className="mf__count">
          {itemsFound} {itemsFound === 1 ? 'ítem encontrado' : 'ítems encontrados'}
        </p>
      )}
    </div>
  );
};
