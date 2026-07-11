const getBaseUrl = () => {
  if (typeof window !== 'undefined') {
    const customUrl = localStorage.getItem('backend_api_url');
    if (customUrl) {
      if (customUrl.includes('.lhr.life')) {
        localStorage.removeItem('backend_api_url');
      } else {
        return customUrl;
      }
    }
  }
  if (typeof window === 'undefined') return 'http://localhost:8081/api/orders';
  const hostname = window.location.hostname;
  const isLocal = hostname === 'localhost' || 
                  hostname === '127.0.0.1' || 
                  /^192\.168\./.test(hostname) || 
                  /^10\./.test(hostname) || 
                  /^172\.(1[6-9]|2[0-9]|3[0-1])\./.test(hostname);
  if (isLocal) {
    return `http://${hostname}:8081/api/orders`;
  }
  // Public HTTPS secure tunnel mapping to local PC backend (stable subdomain)
  return 'https://gourmetflow-backend-arul.loca.lt/api/orders';
};
export const BASE_URL = getBaseUrl();
 
/**
 * Creates a new food order in the Order Service.
 * @param {Object} orderData - The order details (customerName, deliveryAddress, items)
 * @returns {Promise<Object>} The created order response
 */
export async function createOrder(orderData) {
  try {
    const response = await fetch(BASE_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Bypass-Tunnel-Reminder': 'true',
      },
      body: JSON.stringify(orderData),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('API Error in createOrder:', error);
    throw error;
  }
}

/**
 * Fetches the current status and details of an order.
 * @param {number|string} orderId - The unique Order ID
 * @returns {Promise<Object>} The order details
 */
export async function getOrderStatus(orderId) {
  try {
    const response = await fetch(`${BASE_URL}/${orderId}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Bypass-Tunnel-Reminder': 'true',
      }
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error(`API Error in getOrderStatus for ID ${orderId}:`, error);
    throw error;
  }
}
