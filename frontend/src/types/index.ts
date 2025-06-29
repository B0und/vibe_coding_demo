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
  id: number;
  systemName: string;
  eventName: string;
  kafkaTopic: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}

export interface Subscription {
  id: number;
  eventId: number;
  eventName: string;
  systemName: string;
  subscribed: boolean;
}

export interface EventWithSubscription extends Event {
  subscribed: boolean;
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