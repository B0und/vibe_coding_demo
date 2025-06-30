import { create } from 'zustand';
import { authApi, userProfileApi, type AuthUser, type LoginResponse, type UserProfile } from '../api/client';

export interface User {
  id: string;
  username: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;
  updatedAt?: string;
}

interface UserState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  login: (username: string) => Promise<void>;
  logout: () => void;
  fetchUser: () => Promise<void>;
}

export const useUserStore = create<UserState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  login: async (username: string) => {
    set({ isLoading: true, error: null });
    
    try {
      const response: LoginResponse = await authApi.login(username);
      
      // After login, fetch the full profile to get role information
      const profile: UserProfile = await userProfileApi.getProfile();
      
      const user: User = {
        id: profile.id,
        username: profile.username,
        role: profile.role,
        createdAt: profile.createdAt,
        updatedAt: profile.updatedAt,
      };
      
      set({
        user,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Login failed';
      set({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: errorMessage,
      });
      throw error; // Re-throw for component error handling
    }
  },

  logout: () => {
    authApi.logout();
    set({
      user: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
    });
  },

  fetchUser: async () => {
    // Don't fetch if no token exists
    const token = localStorage.getItem('authToken');
    if (!token) {
      return;
    }

    set({ isLoading: true, error: null });
    
    try {
      // Use profile endpoint to get full user information including role
      const profile: UserProfile = await userProfileApi.getProfile();
      
      const user: User = {
        id: profile.id,
        username: profile.username,
        role: profile.role,
        createdAt: profile.createdAt,
        updatedAt: profile.updatedAt,
      };
      
      set({
        user,
        isAuthenticated: true,
        isLoading: false,
        error: null,
      });
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : 'Failed to fetch user';
      set({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        error: errorMessage,
      });
      // Don't throw here as this is often called automatically
    }
  },
}));

// Export a convenience hook that matches the useAuth pattern
export const useAuth = () => {
  const store = useUserStore();
  return store;
}; 