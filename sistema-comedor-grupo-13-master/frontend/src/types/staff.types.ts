export interface StaffFormData {
  firstName: string;
  lastName: string;
  email: string;
  temporaryPassword: string;
  birthDate: string;
  gender: string;
  address: string;
}

export interface StaffResponse {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  birthDate: string;
  gender: string;
  address: string;
  role: string;
}

export interface ApiError {
  response?: {
    data?: {
      error?: string;
      message?: string;
    };
  };
  message?: string;
}
