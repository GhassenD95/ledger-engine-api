import { Link } from 'react-router-dom'
import { Banknote, ArrowLeftRight, FileText } from 'lucide-react'
import { Button } from '../ui/button'

export function Navbar() {
  return (
    <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
      <div className="flex h-16 items-center px-6 max-w-7xl mx-auto">
        <Link to="/" className="flex items-center gap-3 font-bold text-xl">
          <div className="h-8 w-8 rounded-lg bg-primary flex items-center justify-center">
            <Banknote className="h-4 w-4 text-primary-foreground" />
          </div>
          Ledger Engine
        </Link>
        <nav className="ml-auto flex items-center gap-2">
          <Button variant="ghost" size="sm" asChild>
            <Link to="/">
              <Banknote className="h-4 w-4 mr-1" />
              Accounts
            </Link>
          </Button>
          <Button variant="ghost" size="sm" asChild>
            <Link to="/transfer">
              <ArrowLeftRight className="h-4 w-4 mr-1" />
              Transfer
            </Link>
          </Button>
          <Button variant="ghost" size="sm" asChild>
            <Link to="/transaction">
              <FileText className="h-4 w-4 mr-1" />
              Lookup
            </Link>
          </Button>
        </nav>
      </div>
    </header>
  )
}
