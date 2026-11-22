import { useEffect, useMemo, useState } from 'react'
import { BrowserRouter, Link, Navigate, Route, Routes, useLocation, useNavigate, useParams } from 'react-router-dom'
import { ChevronLeft, MapPin, Search, Ticket, UserRound, X } from 'lucide-react'
import './App.css'
import './ui-polish.css'

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
  const navigate = useNavigate()
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
      localStorage.setItem('bms_admin', data.admin ? 'true' : 'false')
      setAuthOpen(false)
      if (data.admin) navigate('/admin')
    } catch (e) { setError(e.message) }
    finally { setLoading(false) }
  }

  function logout() { localStorage.removeItem('bms_token'); localStorage.removeItem('bms_email'); localStorage.removeItem('bms_admin'); window.location.href = '/' }

  const isAdmin = localStorage.getItem('bms_admin') === 'true'

  return <>
    <header className="topbar">
      <Link to="/" className="brand"><span className="brand-mark">b</span><strong>book<span>my</span>show</strong></Link>
      <label className="search"><Search size={19}/><input placeholder="Search for Movies, Theatres" /></label>
      <button className="city-button"><MapPin size={16}/>{city}<ChevronLeft size={14} className="rotate"/></button>
      {userEmail
        ? <div className="profile-actions">{isAdmin && <Link className="admin-link" to="/admin">Admin</Link>}<button className="profile-button" onClick={logout}><UserRound size={17}/>{userEmail.split('@')[0]}</button></div>
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
          <input required type="text" placeholder="Username or email address" value={email} onChange={e => setEmail(e.target.value)} />
          <input required type="password" placeholder="Password" minLength={authMode === 'register' ? 8 : 1} value={password} onChange={e => setPassword(e.target.value)} />
          {error && <div className="form-error">{error}</div>}
          <button className="primary-button" disabled={loading}>{loading ? 'Please wait...' : authMode === 'login' ? 'Sign in' : 'Create account'}</button>
        </form>
      </div>
    </div>}
  </>
}

function MovieCard({ movie }) {
  const themes = {
    'Adventure': 'linear-gradient(145deg,#102f43,#2d7185 55%,#e8ad62)',
    'Thriller': 'linear-gradient(145deg,#11131d,#4b1830 58%,#c84d65)',
    'Romance': 'linear-gradient(145deg,#40243f,#b55c7a 58%,#f1b38d)',
    'Sci-Fi': 'linear-gradient(145deg,#101c3a,#354fa1 58%,#63a8b8)',
    'Comedy': 'linear-gradient(145deg,#71392a,#df9f45 58%,#f3cf77)',
    'Action': 'linear-gradient(145deg,#24130f,#77402e 58%,#d67b45)',
  }
  const gradient = themes[movie.genre] || 'linear-gradient(145deg,#18243b,#5d352f 55%,#c45b3f)'
  return <Link to={`/movie/${movie.id}`} className="movie-card">
    <div className={`poster genre-${String(movie.genre || 'Drama').toLowerCase().replace(/[^a-z]/g,'')}`} style={{ background: gradient }}>
      <span className="poster-rating">★ 9.2</span>
      <div className="poster-backdrop"><i></i><i></i><i></i></div>
      <div className="poster-art"><div className="poster-ring"></div><span>{movie.title}</span></div>
      <div className="poster-meta"><span>{movie.language}</span><span>{movie.genre || 'Drama'}</span></div>
    </div>
    <h3>{movie.title}</h3>
    <p>{movie.genre || 'Drama'} · {movie.durationMinutes} min</p>
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
      <div className={`detail-poster genre-${String(movie.genre || 'Drama').toLowerCase().replace(/[^a-z]/g,'')}`}><div className="poster-backdrop"><i></i><i></i><i></i></div><div className="poster-art"><div className="poster-ring"></div><span>{movie.title}</span></div></div>
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

function AdminLogin() {
  const navigate = useNavigate()
  const [identity, setIdentity] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function login(event) {
    event.preventDefault()
    setError(''); setLoading(true)
    try {
      const data = await api('/api/v1/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email: identity, password })
      })
      if (!data.admin) throw new Error('This account does not have admin access')
      localStorage.setItem('bms_token', data.token)
      localStorage.setItem('bms_email', data.email)
      localStorage.setItem('bms_admin', 'true')
      navigate('/admin')
      window.location.reload()
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return <main className="admin-login-page"><div className="admin-login-card"><span className="brand-mark">b</span><span className="eyebrow">ADMIN PORTAL</span><h1>Welcome back</h1><p>Sign in to manage the BookMyShow catalog.</p><form onSubmit={login}><label>Username or email<input required type="text" value={identity} onChange={e=>setIdentity(e.target.value)} placeholder="admin" /></label><label>Password<input required type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="admin" /></label>{error && <div className="form-error">{error}</div>}<button className="primary-button" disabled={loading}>{loading?'Signing in...':'Sign in to dashboard'}</button></form><Link to="/">← Back to website</Link></div></main>
}

function AdminDashboard() {
  const navigate = useNavigate()
  const [catalog, setCatalog] = useState({ movies: [], theatres: [], screens: [], shows: [] })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [tab, setTab] = useState('movie')
  const [movie, setMovie] = useState({ title:'', durationMinutes:120, language:'Hindi', genre:'', synopsis:'' })
  const [screen, setScreen] = useState({ theatreId:'', name:'', rows:6, seatsPerRow:8, regularPrice:'250', premiumPrice:'350' })
  const [show, setShow] = useState({ movieId:'', screenId:'', startTime:'', regularPrice:'250', premiumPrice:'350' })

  async function loadCatalog() {
    try { setCatalog(await api('/api/v1/admin/catalog')) }
    catch (e) { setError(e.message) }
  }
  useEffect(() => { if (localStorage.getItem('bms_admin') !== 'true') navigate('/') ; else loadCatalog() }, [])

  async function submit(path, body) {
    setError(''); setSuccess('')
    try {
      await api(path, { method:'POST', body:JSON.stringify(body) })
      setSuccess('Saved successfully')
      loadCatalog()
    } catch (e) { setError(e.message) }
  }

  return <main className="admin-page">
    <aside className="admin-sidebar"><Link to="/" className="brand"><span className="brand-mark">b</span><strong>book<span>my</span>show</strong></Link><span className="eyebrow">ADMIN CONTROL</span>
      <nav><button className={tab==='movie'?'active':''} onClick={()=>setTab('movie')}>Add Movie</button><button className={tab==='screen'?'active':''} onClick={()=>setTab('screen')}>Add Screen & Seats</button><button className={tab==='show'?'active':''} onClick={()=>setTab('show')}>Schedule Show</button></nav>
      <Link to="/" className="admin-back">← Back to website</Link>
    </aside>
    <section className="admin-content">
      <div className="admin-heading"><div><span>CATALOG MANAGEMENT</span><h1>Admin Dashboard</h1></div><div className="admin-stats"><b>{catalog.movies.length}</b><small>Movies</small></div></div>
      {error && <div className="form-error">{error}</div>}{success && <div className="success-message">{success}</div>}

      {tab==='movie' && <form className="admin-form" onSubmit={e=>{e.preventDefault();submit('/api/v1/admin/movies',{...movie,durationMinutes:Number(movie.durationMinutes)})}}>
        <h2>Add a Movie</h2><div className="form-grid"><label>Movie title<input required value={movie.title} onChange={e=>setMovie({...movie,title:e.target.value})}/></label><label>Duration (minutes)<input required type="number" min="1" value={movie.durationMinutes} onChange={e=>setMovie({...movie,durationMinutes:e.target.value})}/></label><label>Language<input value={movie.language} onChange={e=>setMovie({...movie,language:e.target.value})}/></label><label>Genre<input required value={movie.genre} onChange={e=>setMovie({...movie,genre:e.target.value})}/></label></div><label>Synopsis<textarea value={movie.synopsis} onChange={e=>setMovie({...movie,synopsis:e.target.value})}/></label><button className="primary-button">Add Movie</button>
      </form>}

      {tab==='screen' && <form className="admin-form" onSubmit={e=>{e.preventEvent?.();e.preventDefault();submit('/api/v1/admin/screens',{...screen,theatreId:Number(screen.theatreId),rows:Number(screen.rows),seatsPerRow:Number(screen.seatsPerRow),regularPrice:Number(screen.regularPrice),premiumPrice:Number(screen.premiumPrice)})}}>
        <h2>Add Screen and Seats</h2><div className="form-grid"><label>Theatre<select required value={screen.theatreId} onChange={e=>setScreen({...screen,theatreId:e.target.value})}><option value="">Select theatre</option>{catalog.theatres.map(t=><option key={t.id} value={t.id}>{t.name}</option>)}</select></label><label>Screen name<input required value={screen.name} onChange={e=>setScreen({...screen,name:e.target.value})}/></label><label>Rows<input required type="number" min="1" max="20" value={screen.rows} onChange={e=>setScreen({...screen,rows:e.target.value})}/></label><label>Seats per row<input required type="number" min="1" max="30" value={screen.seatsPerRow} onChange={e=>setScreen({...screen,seatsPerRow:e.target.value})}/></label><label>Regular seat price (₹)<input required type="number" min="1" value={screen.regularPrice} onChange={e=>setScreen({...screen,regularPrice:e.target.value})}/></label><label>Premium seat price (₹)<input required type="number" min="1" value={screen.premiumPrice} onChange={e=>setScreen({...screen,premiumPrice:e.target.value})}/></label></div><button className="primary-button">Create Screen Layout</button>
      </form>}

      {tab==='show' && <form className="admin-form" onSubmit={e=>{e.preventDefault();submit('/api/v1/admin/shows',{...show,movieId:Number(show.movieId),screenId:Number(show.screenId),regularPrice:Number(show.regularPrice),premiumPrice:Number(show.premiumPrice)})}}>
        <h2>Schedule a Show</h2><div className="form-grid"><label>Movie<select required value={show.movieId} onChange={e=>setShow({...show,movieId:e.target.value})}><option value="">Select movie</option>{catalog.movies.map(m=><option key={m.id} value={m.id}>{m.title}</option>)}</select></label><label>Screen<select required value={show.screenId} onChange={e=>setShow({...show,screenId:e.target.value})}><option value="">Select screen</option>{catalog.screens.map(s=><option key={s.id} value={s.id}>{s.name}</option>)}</select></label><label>Show start time<input required type="datetime-local" value={show.startTime} onChange={e=>setShow({...show,startTime:e.target.value})}/></label><label>Regular price (₹)<input required type="number" min="1" value={show.regularPrice} onChange={e=>setShow({...show,regularPrice:e.target.value})}/></label><label>Premium price (₹)<input required type="number" min="1" value={show.premiumPrice} onChange={e=>setShow({...show,premiumPrice:e.target.value})}/></label></div><button className="primary-button">Schedule Show</button>
      </form>}

      <section className="admin-list"><h2>Current Catalog</h2><div className="catalog-cards"><div><b>{catalog.movies.length}</b><span>Movies</span></div><div><b>{catalog.screens.length}</b><span>Screens</span></div><div><b>{catalog.shows.length}</b><span>Shows</span></div></div></section>
    </section>
  </main>
}

function AdminRoute() {
  return localStorage.getItem('bms_admin') === 'true'
    ? <AdminDashboard />
    : <AdminLogin />
}

export default function App() {
  const [city, setCity] = useState('Gurugram')
  return <BrowserRouter><Header city={city} setCity={setCity}/><Routes>
    <Route path="/" element={<Home city={city}/>}/>
    <Route path="/movie/:id" element={<MovieDetails/>}/>
    <Route path="/show/:id" element={<SeatSelection/>}/>
    <Route path="/admin" element={<AdminRoute/>}/>
    <Route path="/admin-login" element={<AdminLogin/>}/>
    <Route path="*" element={<Navigate to="/"/>}/>
  </Routes></BrowserRouter>
}
