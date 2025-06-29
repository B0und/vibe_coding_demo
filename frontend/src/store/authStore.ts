import { create } from 'zustand';

interface User {
  id: string;
  email: string;
  name: string;
  role: 'admin' | 'user';
  telegramUsername?: string;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (user: User) => void;
  logout: () => void;
  updateUser: (userData: Partial<User>) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  login: (user) => set({ user, isAuthenticated: true }),
  logout: () => set({ user: null, isAuthenticated: false }),
  updateUser: (userData) =>
    set((state) => ({
      user: state.user ? { ...state.user, ...userData } : null,
    })),
}));

// Mock authentication hook for development
export const useAuth = () => {
  const { user, isAuthenticated, login, logout, updateUser } = useAuthStore();

  // Mock login function for development
  const mockLogin = (role: 'admin' | 'user' = 'user') => {
    const mockUser: User = {
      id: '1',
      email: role === 'admin' ? 'admin@example.com' : 'user@example.com',
      name: role === 'admin' ? 'Admin User' : 'Regular User',
      role,
      telegramUsername: '@mockuser',
    };
    login(mockUser);
  };

  return {
    user,
    isAuthenticated,
    login: mockLogin,
    logout,
    updateUser,
  };
}; 