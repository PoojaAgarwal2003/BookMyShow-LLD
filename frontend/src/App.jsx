import { useEffect, useMemo, useState } from 'react'
import { BrowserRouter, Link, Navigate, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft, MapPin, Search, Ticket, UserRound, X } from 'lucide-react'
import './App.css'

const api = async (path, options = {}) => {
  const token = localStorage.getItem('bms_token')
  const response = await fetch(path, {
    ...options,
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  })
  const text = await response.text()
  const data = text ? JSON.parse(text) : null
  if (!response.ok) throw new Error(data?.message || data?.error || 'Something went wrong')
  return data
}

function Header({ city, setCity }) {
  const [authOpen, setAuthOpen] = useState(false)
  const [authMode, setAuthMode] = useState('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [name, setName] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const userEmail = localStorage.getItem('bms_email')

  async function authenticate(event) {
    event.preventDefault()
    setError(''); setLoading(true)
    try {
      const body = authMode === 'register'
        ? { name, email, password }
        : { email, password }
      const data = await api(`/api/v1/auth/${authMode}`, { method: 'POST', body: JSON.stringify(body) })
      localStorage.setItem('bms_token', data.token)
      localStorage.setItem('bms_email', data.email)
      setAuthOpen(false)
    } catch (e) { setError(e.message) }
    finally { setLoading(false) }
  }

  function logout() { localStorage.removeItem('bms_token'); localStorage.removeItem('bms_email'); window.location.reload() }

  return <>
    <header className="topbar">
      <Link to="/" className="brand"><span className="brand-mark">b</span><strong>book<span>my</span>show</strong></Link>
      <label className="search"><Search size={19}/><input placeholder="Search for Movies, Theatres" /></label>
      <button className="city-button"><MapPin size={16}/>{city}<ChevronLeft size={14} className="rotate"/></button>
      {userEmail
        ? <button className="profile-button" onClick={logout}><UserRound size={17}/>{userEmail.split('@')[0]}</button>
        : <button className="signin" onClick={() => setAuthOpen(true)}>Sign in</button>}
    </header>
    {authOpen && <div className="modal-backdrop" onMouseDown={() => setAuthOpen(false)}>
      <div className="auth-modal" onMouseDown={e => e.stopPropagation()}>
        <button className="close" onClick={() => setAuthOpen(false)}><X size={20}/></button>
        <h2>{authMode === 'login' ? 'Welcome back' : 'Create account'}</h2>
        <p>{authMode === 'login' ? 'Sign in to continue booking' : 'Join BookMyShow'}</p>
        <div className="auth-tabs">
          <button className={authMode === 'login' ? 'active' : ''} onClick={() => setAuthMode('login')}>Sign in</button>
          <button className={authMode === 'register' ? 'active' : ''} onClick={() => setAuthMode('register')}>Register</button>
        </div>
        <form onSubmit={authenticate}>
          {authMode === 'register' && <input required placeholder="Full name" value={name} onChange={e => setName(e.target.value)} />}
          <input required type="email" placeholder="Email address" value={email} onChange={e => setEmail(e.target.value)} />
          <input required type="password" placeholder="Password" minLength={8} value={password} onChange={e => setPassword(e.target.value)} />
          {error && <div className="form-error">{error}</div>}
          <button className="primary-button" disabled={loading}>{loading ? 'Please wait...' : authMode === 'login' ? 'Sign in' : 'Create account'}</button>
        </form>
      </div>
    </div>}
  </>
}

function MovieCard({ movie }) {
  const gradients = [
    'linear-gradient(145deg,#18243b,#5d352f 70%,#c45b3f)',
    'linear-gradient(145deg,#28243a,#7a2d5d 60%,#e38b65)',
    'linear-gradient(145deg,#122c33,#4c7a68 65%,#e3c88c)',
  ]
  const gradient = gradients[movie.id % gradients.length]
  return <Link to={`/movie/${movie.id}`} className="movie-card">
    <div className="poster" style={{ background: gradient }}>
      <span className="poster-rating">★ 9.2</span>
      <div className="poster-art"><div className="poster-ring"></div><span>{movie.title}</span></div>
    </div>
    <h3>{movie.title}</h3>
    <p>{movie.genre || 'Drama'}</p>
  </Link>
}

function Home({ city }) {
  const [movies, setMovies] = useState([])
  const [error, setError] = useState('')
  useEffect(() => { api('/api/v1/movies').then(setMovies).catch(e => setError(e.message)) }, [])
  return <main>
    <div className="hero-banner">
      <div className="hero-copy"><span>NOW SHOWING</span><h1>A story worth<br/>watching together.</h1><p>Discover movies and book your perfect seat.</p><a href="#recommended">Explore movies</a></div>
      <div className="hero-cinema"><div className="cinema-light"></div><div className="cinema-screen"></div><div className="cinema-seat left"></div><div className="cinema-seat right"></div></div>
    </div>
    <section className="movie-section" id="recommended">
      <div className="section-heading"><div><span>HAND-PICKED</span><h2>Recommended Movies</h2></div><p>{city}</p></div>
      {error && <div className="form-error">Unable to load movies: {error}</div>}
      <div className="movie-grid">{movies.map(movie => <MovieCard key={movie.id} movie={movie}/>)}</div>
    </section>
  </main>
}

function MovieDetails() {
  const { id } = useParams()
  const [movie, setMovie] = useState(null)
  const [shows, setShows] = useState([])
  const navigate = useNavigate()
  useEffect(() => {
    api(`/api/v1/movies/${id}`).then(setMovie)
    api(`/api/v1/shows?movieId=${id}`).then(setShows)
  }, [id])
  if (!movie) return <main className="loading">Loading movie...</main>
  return <main className="details-page">
    <button className="back-button" onClick={() => navigate(-1)}><ChevronLeft size={18}/> Back</button>
    <section className="movie-detail">
      <div className="detail-poster"><div className="poster-art"><div className="poster-ring"></div><span>{movie.title}</span></div></div>
      <div className="detail-copy"><span className="rating">★ 9.2/10</span><h1>{movie.title}</h1><p className="meta">{movie.language} · {movie.genre} · {movie.durationMinutes} min</p><p className="synopsis">{movie.synopsis}</p></div>
    </section>
    <section className="shows-section"><h2>Select Show</h2>
      <div className="show-list">{shows.map(show => <button key={show.id} className="show-card" onClick={() => navigate(`/show/${show.id}`)}>
        <strong>{new Date(show.startTime).toLocaleTimeString([], {hour:'2-digit',minute:'2-digit'})}</strong>
        <span>{show.screen?.name || 'Screen'}</span><small>{new Date(show.startTime).toLocaleDateString()}</small>
      </button>)}</div>
    </section>
  </main>
}

function SeatSelection() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [seats, setSeats] = useState([])
  const [selected, setSelected] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [booking, setBooking] = useState(null)
  const [paymentDone, setPaymentDone] = useState(false)

  const load = async () => {
    try {
      const data = await api(`/api/v1/shows/${id}/seats`)
      setSeats(data)
    } catch (e) {
      setError(e.message)
    }
  }

  useEffect(() => {
    load()
  }, [id])

  useEffect(() => {
    let cancelled = false

    async function refresh() {
      try {
        const data = await api(`/api/v1/shows/${id}/seats`)
        if (!cancelled) setSeats(data)
      } catch (e) {
        if (!cancelled) setError(e.message)
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    refresh()
    const interval = window.setInterval(refresh, 3000)
    return () => {
      cancelled = true
      window.clearInterval(interval)
    }
  }, [id])

  const rows = useMemo(() => {
    const map = {}
    seats.forEach(item => { const label = item.seat.rowLabel; (map[label] ||= []).push(item) })
    return map
  }, [seats])
  const total = selected.reduce((sum, showSeatId) => sum + Number(seats.find(s => s.id === showSeatId)?.price || 0), 0)

  async function reserve() {
    setError('')
    if (!localStorage.getItem('bms_token')) { setError('Please sign in before reserving seats'); return }
    if (!selected.length) { setError('Select at least one seat'); return }
    try {
      await api('/api/v1/bookings/lock', { method:'POST', body:JSON.stringify({showId:Number(id),showSeatIds:selected}) })
      const key = `booking-${crypto.randomUUID?.() || Date.now()}`
      const created = await api('/api/v1/bookings', { method:'POST', body:JSON.stringify({showId:Number(id),showSeatIds:selected,idempotencyKey:key}) })
      setBooking(created)
    } catch (e) { setError(e.message); load() }
  }

  async function pay() {
    setError('')
    try {
      await api(`/api/v1/payments/booking/${booking.bookingId}`, {method:'POST',body:JSON.stringify({method:'MOCK'})})
      setPaymentDone(true)
    } catch (e) { setError(e.message) }
  }

  if (paymentDone) return <main className="success-page"><div className="success-card"><div className="success-icon"><Ticket size={38}/></div><span>BOOKING CONFIRMED</span><h1>You’re all set!</h1><p>Booking #{booking.bookingId}</p><div className="success-seats">{booking.seatIds?.length} seat{booking.seatIds?.length !== 1 ? 's' : ''} booked</div><button className="primary-button" onClick={() => navigate('/')}>Back to movies</button></div></main>

  if (loading) return <main className="seat-page"><button className="back-button" onClick={() => navigate(-1)}><ChevronLeft size={18}/> Back</button><div className="loading-seat-card"><div className="loading-spinner"></div><h2>Loading seat map</h2><p>Checking the latest availability...</p></div></main>

  return <main className="seat-page">
    <button className="back-button" onClick={() => navigate(-1)}><ChevronLeft size={18}/> Back</button>
    <div className="seat-layout">
      <section className="seat-map"><h2>Select Seats</h2><div className="screen"><span>SCREEN THIS WAY</span></div>
        {Object.entries(rows).map(([row, rowSeats]) => (
          <div className="seat-row" key={row}><b>{row}</b>{rowSeats.map(seat => {
            const unavailable = seat.status !== 'AVAILABLE'
            const chosen = selected.includes(seat.id)
            return <button key={seat.id} disabled={unavailable} className={`seat ${chosen?'chosen':''} ${seat.seat.type==='PREMIUM'?'premium':''}`} onClick={() => setSelected(s => chosen ? s.filter(v=>v!==seat.id) : [...s,seat.id])} title={`${row}${seat.seat.seatNumber} · ₹${seat.price}`}>{seat.seat.seatNumber}</button>
          })}</div>
        ))}
        <div className="legend"><span><i className="sample available"></i>Available</span><span><i className="sample chosen"></i>Selected</span><span><i className="sample unavailable"></i>Unavailable</span></div>
      </section>
      <aside className="booking-summary"><span className="eyebrow">YOUR SELECTION</span><h3>{selected.length} Seat{selected.length!==1?'s':''}</h3>
        {booking ? (
          <div className="locked-booking"><b>Pending booking #{booking.bookingId}</b><p>Status: {booking.status}</p><p>Amount: ₹{booking.totalAmount}</p></div>
        ) : (
          <div className="selected-seats">{selected.length ? selected.map(id => { const s=seats.find(v=>v.id===id); return <div key={id}>{s.seat.rowLabel}{s.seat.seatNumber}<strong>₹{s.price}</strong></div> }) : <p>Select seats from the layout</p>}</div>
        )}
        {error && <div className="form-error">{error}</div>}
        {!booking ? <button className="primary-button summary-button" onClick={reserve}>Continue · ₹{total.toFixed(2)}</button> : <button className="primary-button summary-button" onClick={pay}>Pay ₹{booking.totalAmount}</button>}
      </aside>
    </div>
  </main>
}

export default function App() {
  const [city, setCity] = useState('Gurugram')
  return <BrowserRouter><Header city={city} setCity={setCity}/><Routes>
    <Route path="/" element={<Home city={city}/>}/>
    <Route path="/movie/:id" element={<MovieDetails/>}/>
    <Route path="/show/:id" element={<SeatSelection/>}/>
    <Route path="*" element={<Navigate to="/"/>}/>
  </Routes></BrowserRouter>
}
