import { BrowserRouter, Routes, Route } from 'react-router-dom'
import { Navbar } from './components/layout/Navbar'
import { Dashboard } from './pages/Dashboard'
import { AccountPage } from './pages/AccountPage'
import { TransferPage } from './pages/TransferPage'
import { TransactionLookupPage } from './pages/TransactionLookupPage'

export default function App() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-background">
        <Navbar />
        <main>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/accounts/:id" element={<AccountPage />} />
            <Route path="/transfer" element={<TransferPage />} />
            <Route path="/transaction" element={<TransactionLookupPage />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  )
}
