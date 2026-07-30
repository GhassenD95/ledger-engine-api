import type { Account, AccountEntry, JournalEntry, TransferRequest, TransferResponse } from '../types'

const BASE = '/api/v1'

async function fetchJson<T>(url: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${url}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })
  if (!res.ok) {
    const body = await res.json().catch(() => ({}))
    throw new Error(body.message ?? `Request failed: ${res.status}`)
  }
  return res.json()
}

export function getAccounts(): Promise<Account[]> {
  return fetchJson('/accounts')
}

export function getAccount(id: string): Promise<Account> {
  return fetchJson(`/accounts/${id}`)
}

export function getAccountEntries(id: string, page = 0, size = 10): Promise<{ content: AccountEntry[]; totalElements: number }> {
  return fetchJson(`/accounts/${id}/entries?page=${page}&size=${size}`)
}

export function getTransaction(refId: string): Promise<JournalEntry> {
  return fetchJson(`/transactions/${refId}`)
}

export function executeTransfer(req: TransferRequest): Promise<TransferResponse> {
  return fetchJson('/transfers', {
    method: 'POST',
    body: JSON.stringify(req),
  })
}
