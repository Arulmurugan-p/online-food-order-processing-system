import React, { useState, useEffect, useRef } from 'react'
import { createOrder, getOrderStatus, BASE_URL } from './api'
import { 
  ShoppingBag, 
  MapPin, 
  User, 
  Plus, 
  Minus, 
  Trash2, 
  Activity, 
  CheckCircle2, 
  Clock, 
  ChefHat, 
  Truck, 
  XCircle, 
  RotateCcw, 
  ExternalLink 
} from 'lucide-react'

// Premium pre-defined menu items
const MENU_ITEMS = [
  { id: '1', name: 'Truffle Porcini Pizza', price: 24.99, icon: '🍕' },
  { id: '2', name: 'Dry-Aged Wagyu Burger', price: 18.50, icon: '🍔' },
  { id: '3', name: 'Limoncello Matcha Soufflé', price: 12.00, icon: '🍰' },
  { id: '4', name: 'Gold Leaf Espresso Macchiato', price: 6.50, icon: '☕' },
  { id: '5', name: 'Premium Caviar Roll', price: 520.00, icon: '🍣' },
  { id: '6', name: 'Pan-Seared Foie Gras', price: 45.00, icon: '🥩' },
  { id: '7', name: 'Maine Lobster Roll', price: 38.00, icon: '🦞' },
  { id: '8', name: 'Shaved Black Truffle Fries', price: 15.00, icon: '🍟' },
  { id: '9', name: 'Vintage Champagne Goblet', price: 28.00, icon: '🥂' }
]

function App() {
  const configureBackendUrl = () => {
    const current = localStorage.getItem('backend_api_url') || BASE_URL;
    const newUrl = prompt('Configure Backend API URL:\n(e.g., http://localhost:8081/api/orders or https://xxxx.lhr.life/api/orders)\n\nLeave empty to reset to default automatic resolution:', current);
    if (newUrl !== null) {
      if (newUrl.trim() === '') {
        localStorage.removeItem('backend_api_url');
      } else {
        localStorage.setItem('backend_api_url', newUrl.trim());
      }
      window.location.reload();
    }
  };

  // Order Form State
  const [customerName, setCustomerName] = useState('')
  const [deliveryAddress, setDeliveryAddress] = useState('')
  const [cart, setCart] = useState([])
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [consecutiveErrors, setConsecutiveErrors] = useState(0)

  // Tracking State
  const [activeOrderId, setActiveOrderId] = useState(null)
  const [activeOrder, setActiveOrder] = useState(null)
  const [trackingHistory, setTrackingHistory] = useState([])

  // Polling reference
  const pollingIntervalRef = useRef(null)

  // Load history from localStorage
  useEffect(() => {
    const saved = localStorage.getItem('order_history')
    if (saved) {
      try {
        setTrackingHistory(JSON.parse(saved))
      } catch (e) {
        console.error(e)
      }
    }
  }, [])

  const [connectionStatus, setConnectionStatus] = useState('connecting') // 'connected', 'offline', 'mixed-content'
 
  // Dynamic Backend / ActiveMQ Connection Check
  useEffect(() => {
    const checkConnection = async () => {
      // Detect browser HTTPS Mixed Content security blocks for local backend connection
      if (window.location.protocol === 'https:' && BASE_URL.startsWith('http://')) {
        setConnectionStatus('mixed-content')
        return
      }
 
      try {
        await fetch(`${BASE_URL}/health`, {
          headers: { 
            'Accept': 'application/json',
            'Bypass-Tunnel-Reminder': 'true' 
          }
        })
        setConnectionStatus('connected')
      } catch (e) {
        setConnectionStatus('offline')
      }
    }
 
    checkConnection()
    const interval = setInterval(checkConnection, 5000)
    return () => clearInterval(interval)
  }, [])

  // Save history to localStorage
  const saveToHistory = (orderId, customer) => {
    const entry = { id: orderId, customer, timestamp: new Date().toLocaleTimeString() }
    const updated = [entry, ...trackingHistory.filter(h => h.id !== orderId)].slice(0, 5)
    setTrackingHistory(updated)
    localStorage.setItem('order_history', JSON.stringify(updated))
  }

  // Polling logic for Active Order
  useEffect(() => {
    if (activeOrderId) {
      // Immediate fetch
      fetchOrderUpdate(activeOrderId)

      // Start interval to poll every 2 seconds (2000 ms)
      pollingIntervalRef.current = setInterval(() => {
        fetchOrderUpdate(activeOrderId)
      }, 2000)
    }

    // Clean up interval on unmount or when activeOrderId changes
    return () => {
      stopPolling()
    }
  }, [activeOrderId])

  const stopPolling = () => {
    if (pollingIntervalRef.current) {
      clearInterval(pollingIntervalRef.current)
      pollingIntervalRef.current = null
    }
  }

  const fetchOrderUpdate = async (id) => {
    try {
      const data = await getOrderStatus(id)
      setActiveOrder(data)
      setConsecutiveErrors(0)
      setError(null)
      
      // Stop polling if order has reached a terminal state
      if (data.status === 'DELIVERED' || data.status === 'CANCELLED') {
        stopPolling()
      }
    } catch (err) {
      console.error('Error fetching order update:', err)
      setConsecutiveErrors(prev => {
        const next = prev + 1
        if (next >= 5) {
          stopPolling()
          setError('Connection error: Lost connection to Order Service after multiple attempts. Please check if the backend is running.')
        } else {
          setError(`Connection error: Retrying... (Attempt ${next}/5)`)
        }
        return next
      })
    }
  }

  // Cart Helpers
  const addToCart = (menuItem) => {
    setCart(prevCart => {
      const existing = prevCart.find(item => item.id === menuItem.id)
      if (existing) {
        return prevCart.map(item => 
          item.id === menuItem.id ? { ...item, quantity: item.quantity + 1 } : item
        )
      }
      return [...prevCart, { ...menuItem, quantity: 1 }]
    })
  }

  const updateQuantity = (itemId, change) => {
    setCart(prevCart => 
      prevCart.map(item => {
        if (item.id === itemId) {
          const newQty = item.quantity + change
          return newQty > 0 ? { ...item, quantity: newQty } : null
        }
        return item
      }).filter(Boolean)
    )
  }

  const removeFromCart = (itemId) => {
    setCart(prevCart => prevCart.filter(item => item.id !== itemId))
  }

  const cartTotal = cart.reduce((total, item) => total + (item.price * item.quantity), 0)

  // Submit Order Form
  const handlePlaceOrder = async (e) => {
    e.preventDefault()
    if (cart.length === 0) {
      setError('Please add at least one item to your order.')
      return
    }
    
    setError(null)
    setIsSubmitting(true)

    const orderPayload = {
      customerName,
      deliveryAddress,
      items: cart.map(item => ({
        itemName: item.name,
        quantity: item.quantity,
        price: item.price
      }))
    }

    try {
      const response = await createOrder(orderPayload)
      setActiveOrderId(response.id)
      setActiveOrder(response)
      saveToHistory(response.id, response.customerName)
      
      // Reset form
      setCustomerName('')
      setDeliveryAddress('')
      setCart([])
    } catch (err) {
      setError(err.message || 'Failed to place order. Ensure Order Service is running.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const selectHistoryOrder = (id) => {
    setError(null)
    setConsecutiveErrors(0)
    setActiveOrderId(id)
    setActiveOrder(null) // Clear old details while loading
  }

  // Stepper helper
  const getStepStatusClass = (stepName) => {
    if (!activeOrder) return 'step-waiting'
    const status = activeOrder.status

    if (status === 'CANCELLED') {
      return stepName === 'PENDING' ? 'step-completed' : 'step-cancelled'
    }

    const stepOrder = ['PENDING', 'PAID', 'PREPARING', 'DELIVERED']
    const targetIdx = stepOrder.indexOf(stepName)

    let activeStepIdx = -1
    if (status === 'PENDING') {
      activeStepIdx = 1
    } else if (status === 'PAID') {
      activeStepIdx = 2
    } else if (status === 'PREPARING') {
      activeStepIdx = 3
    } else if (status === 'DELIVERED') {
      activeStepIdx = -1
    }

    if (status === 'DELIVERED' || targetIdx < activeStepIdx) return 'step-completed'
    if (targetIdx === activeStepIdx) return 'step-active'
    return 'step-waiting'
  }

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      
      {/* Premium Header */}
      <header className="glass-panel" style={{ margin: '20px', padding: '16px 30px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderRadius: '12px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <span style={{ fontSize: '2rem' }}>🍕</span>
          <div>
            <h1 style={{ margin: 0, fontSize: '1.5rem', background: 'linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              GourmetFlow
            </h1>
            <p style={{ margin: 0, fontSize: '0.75rem', color: '#94a3b8' }}>Event-Driven Orchestrated Kitchen System</p>
          </div>
        </div>
        <div 
          onClick={configureBackendUrl}
          style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}
          title="Click to configure backend URL"
        >
          <Activity size={16} color={connectionStatus === 'connected' ? "#10b981" : connectionStatus === 'mixed-content' ? "#eab308" : "#f87171"} />
          <span style={{ fontSize: '0.85rem', color: connectionStatus === 'connected' ? "#10b981" : connectionStatus === 'mixed-content' ? "#eab308" : "#f87171", fontWeight: 600 }}>
            {connectionStatus === 'connected' && "ActiveMQ Connected"}
            {connectionStatus === 'offline' && "ActiveMQ Offline"}
            {connectionStatus === 'mixed-content' && (
              <span>
                HTTPS Block: Use local HTTP (e.g. <a href="http://localhost:5173" style={{ textDecoration: 'underline', color: 'inherit' }}>Vite Link</a> or <a href="http://127.0.0.1:5500/standalone-dashboard.html" style={{ textDecoration: 'underline', color: 'inherit' }}>HTML Link</a>)
              </span>
            )}
          </span>
        </div>
      </header>

      {/* Main Content Dashboard */}
      <main style={{ flex: 1, padding: '0 20px 40px 20px', display: 'grid', gridTemplateColumns: '1.2fr 1fr', gap: '20px' }}>
        
        {/* Left Column: Form & Menu */}
        <section style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          {/* Create Order Card */}
          <div className="glass-panel" style={{ padding: '24px' }}>
            <h2 style={{ marginTop: 0, marginBottom: '20px', fontSize: '1.3rem', display: 'flex', alignItems: 'center', gap: '10px' }}>
              <ShoppingBag size={20} color="#3b82f6" /> Place New Order
            </h2>

            {error && (
              <div style={{ padding: '12px', background: 'rgba(239, 68, 68, 0.15)', border: '1px solid rgba(239, 68, 68, 0.3)', borderRadius: '8px', color: '#f87171', marginBottom: '16px', fontSize: '0.9rem', display: 'flex', flexDirection: 'column', gap: '8px', alignItems: 'flex-start' }}>
                <span>{error}</span>
                {consecutiveErrors >= 5 && activeOrderId && (
                  <button 
                    type="button" 
                    onClick={() => {
                      setConsecutiveErrors(0);
                      setError(null);
                      // Start polling again
                      setActiveOrderId(activeOrderId);
                    }}
                    className="btn-primary"
                    style={{ padding: '6px 12px', fontSize: '0.8rem', width: 'auto', background: '#3b82f6', border: 'none', borderRadius: '6px', color: '#fff', cursor: 'pointer' }}
                  >
                    Retry Connection
                  </button>
                )}
              </div>
            )}

            <form onSubmit={handlePlaceOrder} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.5fr', gap: '16px' }}>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <label style={{ fontSize: '0.85rem', color: '#94a3b8', fontWeight: 500 }}>Customer Name</label>
                  <div style={{ position: 'relative' }}>
                    <User size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#64748b' }} />
                    <input 
                      type="text" 
                      placeholder="Jane Doe" 
                      className="glass-input" 
                      style={{ width: '100%', paddingLeft: '36px', boxSizing: 'border-box' }}
                      value={customerName}
                      onChange={(e) => setCustomerName(e.target.value)}
                      required
                    />
                  </div>
                </div>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                  <label style={{ fontSize: '0.85rem', color: '#94a3b8', fontWeight: 500 }}>Delivery Address</label>
                  <div style={{ position: 'relative' }}>
                    <MapPin size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: '#64748b' }} />
                    <input 
                      type="text" 
                      placeholder="123 Luxury Lane, Foodie City" 
                      className="glass-input" 
                      style={{ width: '100%', paddingLeft: '36px', boxSizing: 'border-box' }}
                      value={deliveryAddress}
                      onChange={(e) => setDeliveryAddress(e.target.value)}
                      required
                    />
                  </div>
                </div>
              </div>

              {/* Menu Grid */}
              <div style={{ marginTop: '10px' }}>
                <h3 style={{ margin: '0 0 12px 0', fontSize: '1rem', color: '#94a3b8' }}>Gourmet Selection (Click to add)</h3>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '10px' }}>
                  {MENU_ITEMS.map(item => (
                    <button
                      key={item.id}
                      type="button"
                      onClick={() => addToCart(item)}
                      style={{
                        background: 'rgba(255, 255, 255, 0.03)',
                        border: '1px solid rgba(255, 255, 255, 0.06)',
                        borderRadius: '10px',
                        padding: '12px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        textAlign: 'left',
                        cursor: 'pointer',
                        transition: 'all 0.2s ease',
                      }}
                      onMouseEnter={(e) => {
                        e.currentTarget.style.background = 'rgba(59, 130, 246, 0.08)'
                        e.currentTarget.style.borderColor = 'rgba(59, 130, 246, 0.3)'
                      }}
                      onMouseLeave={(e) => {
                        e.currentTarget.style.background = 'rgba(255, 255, 255, 0.03)'
                        e.currentTarget.style.borderColor = 'rgba(255, 255, 255, 0.06)'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <span style={{ fontSize: '1.25rem' }}>{item.icon}</span>
                        <div>
                          <p style={{ margin: 0, fontSize: '0.85rem', fontWeight: 600, color: '#f1f5f9' }}>{item.name.split(' (')[0]}</p>
                          <p style={{ margin: 0, fontSize: '0.75rem', color: '#3b82f6', fontWeight: 500 }}>${item.price.toFixed(2)}</p>
                        </div>
                      </div>
                      <Plus size={16} color="#64748b" />
                    </button>
                  ))}
                </div>
              </div>

              {/* Order Cart list */}
              {cart.length > 0 && (
                <div style={{ background: 'rgba(0, 0, 0, 0.2)', padding: '16px', borderRadius: '10px', border: '1px solid rgba(255, 255, 255, 0.05)' }}>
                  <h3 style={{ margin: '0 0 10px 0', fontSize: '0.9rem', color: '#94a3b8' }}>Order Summary</h3>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', maxHeight: '160px', overflowY: 'auto' }}>
                    {cart.map(item => (
                      <div key={item.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.85rem' }}>
                        <span style={{ flex: 1, color: '#cbd5e1' }}>{item.name}</span>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <button type="button" onClick={() => updateQuantity(item.id, -1)} style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', display: 'flex' }}><Minus size={14} /></button>
                            <span style={{ fontWeight: 600 }}>{item.quantity}</span>
                            <button type="button" onClick={() => addToCart(item)} style={{ background: 'none', border: 'none', color: '#94a3b8', cursor: 'pointer', display: 'flex' }}><Plus size={14} /></button>
                          </div>
                          <span style={{ width: '50px', textAlign: 'right', fontWeight: 600 }}>${(item.price * item.quantity).toFixed(2)}</span>
                          <button type="button" onClick={() => removeFromCart(item.id)} style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', display: 'flex' }}><Trash2 size={14} /></button>
                        </div>
                      </div>
                    ))}
                  </div>

                  <div style={{ borderTop: '1px solid rgba(255, 255, 255, 0.1)', marginTop: '12px', paddingTop: '10px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontWeight: 600, color: '#94a3b8' }}>Total Amount</span>
                    <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end' }}>
                      <span style={{ fontWeight: 700, fontSize: '1.2rem', color: '#10b981' }}>
                        ${cartTotal.toFixed(2)}
                      </span>
                    </div>
                  </div>
                </div>
              )}

              <button
                type="submit"
                disabled={isSubmitting || cart.length === 0}
                className="btn-primary"
                style={{ marginTop: '10px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
              >
                {isSubmitting ? 'Submitting Order...' : 'Place Dispatch Order'}
              </button>
            </form>
          </div>
        </section>

        {/* Right Column: Status & History */}
        <section style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          {/* Active Tracker Card */}
          <div className="glass-panel" style={{ padding: '24px', flex: 1, display: 'flex', flexDirection: 'column' }}>
            <h2 style={{ marginTop: 0, marginBottom: '20px', fontSize: '1.3rem', display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Clock size={20} color="#3b82f6" /> Real-time Tracker
            </h2>

            {!activeOrderId ? (
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#64748b', textAlign: 'center', padding: '20px' }}>
                <ShoppingBag size={48} style={{ strokeWidth: 1.5, marginBottom: '12px', opacity: 0.5 }} />
                <p style={{ margin: 0, fontSize: '0.95rem' }}>No active order being tracked.</p>
                <p style={{ margin: 0, fontSize: '0.8rem', color: '#475569', marginTop: '4px' }}>Place an order or select a recent one below to monitor progress.</p>
              </div>
            ) : !activeOrder ? (
              <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#64748b' }}>
                <div style={{ width: '30px', height: '30px', border: '3px solid rgba(59, 130, 246, 0.2)', borderTopColor: '#3b82f6', borderRadius: '50%', animation: 'spin 1s linear infinite', marginBottom: '12px' }}></div>
                <span>Retrieving dispatch status...</span>
                <style>{`
                  @keyframes spin { to { transform: rotate(360deg); } }
                `}</style>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', height: '100%' }}>
                
                {/* Order Summary & Status Badge */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(0, 0, 0, 0.15)', padding: '16px', borderRadius: '10px', border: '1px solid rgba(255, 255, 255, 0.04)' }}>
                  <div>
                    <span style={{ fontSize: '0.75rem', color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Order Reference</span>
                    <h3 style={{ margin: '2px 0 0 0', fontSize: '1.1rem', color: '#f1f5f9' }}>Order #{activeOrder.id}</h3>
                    <p style={{ margin: '4px 0 0 0', fontSize: '0.8rem', color: '#64748b' }}>Customer: {activeOrder.customerName}</p>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '6px' }}>
                    <span className={`badge-status status-${activeOrder.status.toLowerCase()}`}>
                      {activeOrder.status === 'PENDING' && <Clock size={12} />}
                      {activeOrder.status === 'PAID' && <CheckCircle2 size={12} />}
                      {activeOrder.status === 'PREPARING' && <ChefHat size={12} />}
                      {activeOrder.status === 'DELIVERED' && <Truck size={12} />}
                      {activeOrder.status === 'CANCELLED' && <XCircle size={12} />}
                      {activeOrder.status}
                    </span>
                    <span style={{ fontSize: '0.8rem', color: '#94a3b8', fontStyle: 'italic' }}>Poll: Every 2s</span>
                  </div>
                </div>

                {/* Camunda BPMN Stepper */}
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px', padding: '0 8px' }}>
                  
                  {/* Step 1: PENDING */}
                  <div style={{ display: 'flex', gap: '16px', position: 'relative' }}>
                    <div style={{ position: 'absolute', left: '15px', top: '30px', bottom: '-20px', width: '2px', background: 'rgba(255, 255, 255, 0.08)' }}></div>
                    <div className={`step-circle ${getStepStatusClass('PENDING')}`}>
                      <Clock size={16} />
                    </div>
                    <div>
                      <h4 style={{ margin: 0, fontSize: '0.9rem', color: '#f1f5f9' }}>Order Created & Placed</h4>
                      <p style={{ margin: '4px 0 0 0', fontSize: '0.75rem', color: '#94a3b8' }}>Order registered in local database. Launching Saga transaction workflow.</p>
                    </div>
                  </div>

                  {/* Step 2: PAID */}
                  <div style={{ display: 'flex', gap: '16px', position: 'relative' }}>
                    <div style={{ position: 'absolute', left: '15px', top: '30px', bottom: '-20px', width: '2px', background: 'rgba(255, 255, 255, 0.08)' }}></div>
                    <div className={`step-circle ${getStepStatusClass('PAID')}`}>
                      <CheckCircle2 size={16} />
                    </div>
                    <div style={{ flex: 1 }}>
                      <h4 style={{ margin: 0, fontSize: '0.9rem', color: '#f1f5f9' }}>Payment Verification</h4>
                      <p style={{ margin: '4px 0 0 0', fontSize: '0.75rem', color: '#94a3b8' }}>
                        {activeOrder.status === 'CANCELLED' 
                          ? 'Payment transaction failed. Order cancelled.' 
                          : 'Validating credit authorizations and accounts.'}
                      </p>
                    </div>
                  </div>

                  {/* Step 3: PREPARING */}
                  <div style={{ display: 'flex', gap: '16px', position: 'relative' }}>
                    <div style={{ position: 'absolute', left: '15px', top: '30px', bottom: '-20px', width: '2px', background: 'rgba(255, 255, 255, 0.08)' }}></div>
                    <div className={`step-circle ${getStepStatusClass('PREPARING')}`}>
                      <ChefHat size={16} />
                    </div>
                    <div>
                      <h4 style={{ margin: 0, fontSize: '0.9rem', color: '#f1f5f9' }}>Kitchen Preparation</h4>
                      <p style={{ margin: '4px 0 0 0', fontSize: '0.75rem', color: '#94a3b8' }}>Food being cooked and packaged by culinary professionals.</p>
                    </div>
                  </div>

                  {/* Step 4: DELIVERED */}
                  <div style={{ display: 'flex', gap: '16px' }}>
                    <div className={`step-circle ${getStepStatusClass('DELIVERED')}`}>
                      <Truck size={16} />
                    </div>
                    <div>
                      <h4 style={{ margin: 0, fontSize: '0.9rem', color: '#f1f5f9' }}>Courier Dispatch & Complete</h4>
                      <p style={{ margin: '4px 0 0 0', fontSize: '0.75rem', color: '#94a3b8' }}>Assigned to courier. Delivered safely to client residence.</p>
                    </div>
                  </div>

                </div>

                {activeOrder.status === 'CANCELLED' && (
                  <div style={{ padding: '12px', background: 'rgba(239, 68, 68, 0.08)', border: '1px solid rgba(239, 68, 68, 0.2)', borderRadius: '8px', color: '#fca5a5', fontSize: '0.8rem', display: 'flex', flexDirection: 'column', gap: '4px' }}>
                    <span style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '6px' }}>Saga Rollback Triggered</span>
                    <span>The transaction was cancelled or failed, and the Camunda workflow process engine successfully completed compensation rollbacks across participants.</span>
                  </div>
                )}

                {/* Reset button */}
                <button
                  type="button"
                  onClick={() => setActiveOrderId(null)}
                  className="btn-secondary"
                  style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', fontSize: '0.85rem' }}
                >
                  <RotateCcw size={16} /> Track a Different Order
                </button>
              </div>
            )}
          </div>

          {/* Recent Orders List Card */}
          <div className="glass-panel" style={{ padding: '20px' }}>
            <h3 style={{ margin: '0 0 12px 0', fontSize: '1rem', color: '#94a3b8' }}>Recent Orders</h3>
            {trackingHistory.length === 0 ? (
              <p style={{ margin: 0, fontSize: '0.8rem', color: '#64748b' }}>No recent order logs saved.</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {trackingHistory.map(history => (
                  <button
                    key={history.id}
                    onClick={() => selectHistoryOrder(history.id)}
                    style={{
                      background: activeOrderId === history.id ? 'rgba(59, 130, 246, 0.1)' : 'rgba(255, 255, 255, 0.02)',
                      border: activeOrderId === history.id ? '1px solid rgba(59, 130, 246, 0.3)' : '1px solid rgba(255, 255, 255, 0.06)',
                      borderRadius: '8px',
                      padding: '10px 14px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      cursor: 'pointer',
                      textAlign: 'left',
                      transition: 'all 0.2s ease',
                      width: '100%'
                    }}
                  >
                    <div>
                      <p style={{ margin: 0, fontSize: '0.85rem', fontWeight: 600, color: '#f1f5f9' }}>Order #{history.id}</p>
                      <p style={{ margin: 0, fontSize: '0.75rem', color: '#94a3b8' }}>{history.customer} • {history.timestamp}</p>
                    </div>
                    <ExternalLink size={14} color="#64748b" />
                  </button>
                ))}
              </div>
            )}
          </div>

        </section>

      </main>

      {/* Embedded inline CSS for specific React UI layout adjustments */}
      <style>{`
        .step-circle {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 2;
          transition: all 0.3s ease;
        }
        .step-waiting {
          background: #1e293b;
          border: 2px solid #475569;
          color: #64748b;
        }
        .step-active {
          background: #1e3a8a;
          border: 2px solid #3b82f6;
          color: #60a5fa;
          box-shadow: 0 0 10px rgba(59, 130, 246, 0.4);
          animation: pulse-border 1.5s infinite alternate;
        }
        .step-completed {
          background: #064e3b;
          border: 2px solid #10b981;
          color: #34d399;
        }
        .step-cancelled {
          background: #7f1d1d;
          border: 2px solid #ef4444;
          color: #f87171;
        }
        
        @keyframes pulse-border {
          0% { border-color: #3b82f6; }
          100% { border-color: #60a5fa; box-shadow: 0 0 14px rgba(59, 130, 246, 0.6); }
        }
      `}</style>
    </div>
  )
}

export default App
