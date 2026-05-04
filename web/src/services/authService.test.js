jest.mock('axios', () => ({
  __esModule: true,
  default: {
    post: jest.fn()
  }
}));

import axios from 'axios';
import {
  register,
  login,
  logout,
  getCurrentUser,
  isAuthenticated,
  getAuthToken
} from './authService';

describe('authService', () => {
  beforeEach(() => {
    localStorage.clear();
    jest.clearAllMocks();
  });

  test('register stores user and token when successful', async () => {
    const payload = { id: 1, name: 'Test', email: 'test@unigear.com', accessToken: 'token123' };
    axios.post.mockResolvedValueOnce({ data: payload });

    const result = await register('Test', 'test@unigear.com', 'password123');

    expect(result.success).toBe(true);
    expect(getCurrentUser()).toEqual(payload);
    expect(getAuthToken()).toBe('token123');
    expect(isAuthenticated()).toBe(true);
  });

  test('login returns error and clears token on failure', async () => {
    localStorage.setItem('token', 'old-token');
    axios.post.mockRejectedValueOnce({
      response: {
        data: {
          error: 'Invalid email or password'
        }
      }
    });

    const result = await login('test@unigear.com', 'wrong');

    expect(result.success).toBe(false);
    expect(result.error).toBe('Invalid email or password');
    expect(getAuthToken()).toBeNull();
  });

  test('logout clears local storage auth state', () => {
    localStorage.setItem('user', JSON.stringify({ id: 1 }));
    localStorage.setItem('token', 'token123');

    logout();

    expect(getCurrentUser()).toBeNull();
    expect(getAuthToken()).toBeNull();
    expect(isAuthenticated()).toBe(false);
  });
});
