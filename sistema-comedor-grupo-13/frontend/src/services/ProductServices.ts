import { BASE_API_URL } from "@/config/app-query-client";
import { Ingredient } from "./ingredientAPI"; 

// Valid product categories matching backend ProductCategory enum
export const PRODUCT_CATEGORIES = {
  SANDWICH: "Sándwich",
  DRINK: "Bebida",
  DESSERT: "Postre",
  SALAD: "Ensalada",
  MAIN_COURSE: "Plato Principal",
  COMBO: "Combo",
  SIDE_DISH: "Acompañamiento",
  COFFEE: "Café"
} as const;

export type ProductCategory = keyof typeof PRODUCT_CATEGORIES;

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  available: boolean;
  active: boolean;
  productType: 'SIMPLE' | 'ELABORATE'; 
  stock?: number; 
  ingredients: Ingredient[];
  ingredientQuantities: Record<string, number>;
  image?: string;
  inStock: boolean;
  category?: string;
}

// For creating simple products
export interface CreateSimpleProductDTO {
  name: string;
  description: string;
  price: number;
  active: boolean;
  category: ProductCategory;
  stock: number;
}

// For creating elaborate products
export interface CreateElaborateProductDTO {
  name: string;
  description: string;
  price: number;
  active: boolean;
  category: ProductCategory;
  stock: number;
  ingredientIds: number[];
  ingredientQuantities: Record<number, number>;
  // Optional payload shape that will be sent to the backend: map of ingredientId -> quantity
  ingredients?: Record<string, number>;
}

// Generic DTO for frontend use
export interface CreateProductDTO {
  name: string;
  description: string;
  price: number;
  available: boolean;
  productType: 'SIMPLE' | 'ELABORATE';
  category: ProductCategory;
  stock?: number; 
  ingredientIds: number[];
  ingredientQuantities: Record<number, number>;
  image?: string;
  // Optional backend-ready map of ingredientId -> quantity (string keys as JSON objects)
  ingredients?: Record<string, number>;
}

export interface UpdateProductDTO {
  name?: string;
  description?: string;
  price?: number;
  available?: boolean;
  ingredientIds?: number[];
  stock?: number;
  ingredientQuantities?: Record<number, number>; 
  ingredients?: Record<string, number>;
  image?: string;
}

export interface UpdateProductPriceDTO {
  price: number;
}

export interface UpdateProductDescriptionDTO {
  description: string;
}

export interface DeleteProductRequest {
  reason?: string;
}

class ProductAPI {
  private baseUrl = `${BASE_API_URL}/products`;

  private mapProduct(raw: Record<string, unknown>): Product {
    return {
      id: raw.id,
      name: raw.name,
      description: raw.description,
      price: raw.price,
      available: raw.available,
      active: raw.active,
      productType: raw.type ?? raw.productType,
      stock: raw.stock,
      ingredients: raw.ingredients ?? [],
      ingredientQuantities: raw.ingredientQuantities ?? (raw.ingredientQuantities || {}),
      image: raw.imageUrl ?? raw.image,
      inStock: raw.inStock ?? (raw.available && (((raw.stock as number) ?? 0) > 0)),
      category: raw.category
    } as Product;
  }

  private getAuthHeaders(token?: string): HeadersInit {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }
    
    return headers;
  }

  async getAll(token?: string): Promise<Product[]> {
    const response = await fetch(this.baseUrl + "/all", {
      method: 'GET',
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      throw new Error(`Error obteniendo productos: ${response.statusText}`);
    }

    const json = await response.json() as Array<Record<string, unknown>>;
    const products = Array.isArray(json) ? json.map((r: Record<string, unknown>) => this.mapProduct(r)) : [];
    return products.filter(product => product.category !== 'COMBO');
  }

  async getProducts(token?: string): Promise<Product[]> {
    const response = await fetch(this.baseUrl, {
      method: 'GET',
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      throw new Error(`Error obteniendo productos: ${response.statusText}`);
    }

    const json = await response.json() as Array<Record<string, unknown>>;
    const products = Array.isArray(json) ? json.map((r: Record<string, unknown>) => this.mapProduct(r)) : [];
    // Filtrar productos de tipo COMBO
    return products.filter(product => product.category !== 'COMBO');
  }

  async getById(id: number, token?: string): Promise<Product> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'GET',
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      throw new Error(`Error obteniendo producto: ${response.statusText}`);
    }

    const json = await response.json();
    return this.mapProduct(json);

  }

  async activate(id: number, token?: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/${id}/activate`, {
      method: 'PUT',
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error activando producto');
    }
  }
  

  async setProducts(products: Product[], token?: string): Promise<Product[]> {
    // Update each product individually using the PUT endpoint
    const updatePromises = products.map(product => 
      this.updateProduct(product.id, {
        name: product.name,
        description: product.description,
        price: product.price,
        available: product.available,
        ingredientIds: product.ingredients.map(ing => ing.id),
        ingredientQuantities: this.convertToIngredientQuantities(product),
        image: product.image
      }, token)
    );

    return Promise.all(updatePromises);
  }

  private convertToIngredientQuantities(product: Product): Record<number, number> {
    const quantities: Record<number, number> = {};
    
    if (product.ingredientQuantities) {
      // Convert from ingredient name-based quantities to ID-based quantities
      Object.entries(product.ingredientQuantities).forEach(([ingredientName, quantity]) => {
        const ingredient = product.ingredients.find(ing => ing.name === ingredientName);
        if (ingredient) {
          quantities[ingredient.id] = quantity;
        }
      });
    }
    
    return quantities;
  }

  async updateProduct(id: number, dto: UpdateProductDTO, token?: string): Promise<Product> {
    const response = await fetch(`${this.baseUrl}/${id}`, {
      method: 'PUT',
      headers: this.getAuthHeaders(token),
      body: JSON.stringify(dto),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error actualizando producto');
    }

    const json = await response.json();
    return this.mapProduct(json);
  }

  async create(dto: CreateProductDTO, token?: string): Promise<Product> {
    // Determine the endpoint based on product type
    const endpoint = dto.productType === 'SIMPLE' ? 
      `${this.baseUrl}/simple` : 
      `${this.baseUrl}/elaborate`;
    
    // Transform the frontend DTO to match backend expectations
    // Backend expects for elaborate products a single `ingredients` map: { <ingredientId>: <quantity> }
    const backendDTO = dto.productType === 'SIMPLE' ? {
      name: dto.name,
      description: dto.description,
      price: dto.price,
      active: dto.available,
      category: dto.category,
      stock: dto.stock || 0
    } : {
      name: dto.name,
      description: dto.description,
      price: dto.price,
      active: dto.available,
      category: dto.category,
      // Build `ingredients` as a map from ingredientId -> quantity (numbers).
      ingredients: Object.fromEntries(
        Object.entries(dto.ingredientQuantities || {})
          .filter(([, q]) => q !== undefined && q !== null && !isNaN(Number(q)) && Number(q) > 0)
          .map(([id, q]) => [id, Number(q)])
      )
    };
    
    const response = await fetch(endpoint, {
      method: 'POST',
      headers: this.getAuthHeaders(token),
      body: JSON.stringify(backendDTO),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error creando producto');
    }

    const json = await response.json();
    return this.mapProduct(json);
  }

  async updatePrice(id: number, price: number, token?: string): Promise<Product> {
    const response = await fetch(`${this.baseUrl}/${id}/price`, {
      method: 'PUT',
      headers: this.getAuthHeaders(token),
      body: JSON.stringify({ price }),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error actualizando precio');
    }

    const json = await response.json();
    return this.mapProduct(json);
  }

  async updateStock(id: number, stock: number, token?: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/${id}/stock`, {
      method: 'PUT',
      headers: this.getAuthHeaders(token),
      body: JSON.stringify({ stock }),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error actualizando stock');
    }

    return;
  }

  async updateName(id: number, name: string, token?: string): Promise<Product> {
  const response = await fetch(`${this.baseUrl}/${id}/name`, {
    method: 'PUT',
    headers: this.getAuthHeaders(token),
    body: JSON.stringify({ name }),
  });

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
    }
    const error = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(error.message || 'Error actualizando nombre');
  }

  const json = await response.json();
  return this.mapProduct(json);
}

  async updateDescription(id: number, description: string, token?: string): Promise<Product> {
    const response = await fetch(`${this.baseUrl}/${id}/description`, {
      method: 'PUT',
      headers: this.getAuthHeaders(token),
      body: JSON.stringify({ description }),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error actualizando descripción');
    }

    const json = await response.json();
    return this.mapProduct(json);
  }
  
  async deactivate(id: number, reason?: string, token?: string): Promise<void> {
    const response = await fetch(`${this.baseUrl}/${id}/deactivate`, {
      method: 'PUT',
      headers: this.getAuthHeaders(token),
      body: reason ? JSON.stringify({ reason }) : undefined,
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error desactivando producto');
    }
  }

  async restore(id: number, token?: string): Promise<Product> {
    const response = await fetch(`${this.baseUrl}/${id}/activate`, {
      method: 'PUT',
      headers: this.getAuthHeaders(token),
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error restaurando producto');
    }

    const json = await response.json();
    return this.mapProduct(json);
  }

  async updateImage(id: number, file: File, token?: string): Promise<Product> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await fetch(`${this.baseUrl}/${id}/image`, {
      method: 'PUT',
      headers: {
        'Authorization': token ? `Bearer ${token}` : '',
      },
      body: formData,
    });

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No autorizado. Por favor, inicie sesión nuevamente.');
      }
      const error = await response.json().catch(() => ({ message: response.statusText }));
      throw new Error(error.message || 'Error actualizando imagen');
    }

    const json = await response.json();
    return this.mapProduct(json);
  }

}

export const productAPI = new ProductAPI();