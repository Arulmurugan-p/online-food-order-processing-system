const BACKEND_HOST = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
const BASE_URL = `http://${BACKEND_HOST}:8081/api/orders`;

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
