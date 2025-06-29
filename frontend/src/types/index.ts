export interface User {
  id: string;
  email: string;
  name: string;
  role: 'admin' | 'user';
  telegramUsername?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Event {
  id: string;
  title: string;
  description: string;
  eventDate: string;
  location?: string;
  maxParticipants?: number;
  currentParticipants: number;
  status: 'active' | 'cancelled' | 'completed';
  createdAt: string;
  updatedAt: string;
}

export interface Subscription {
  id: string;
  userId: string;
  eventId: string;
  status: 'active' | 'cancelled';
  subscribedAt: string;
  event?: Event;
  user?: User;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  data: T[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
} 