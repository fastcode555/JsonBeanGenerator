export interface User {
  user_id?: number
  user_name?: string
  is_vip?: boolean
  balance?: number
  tags?: string[]
  scores?: number[][]
  empty_list?: EmptyList[]
  address?: Address
  orders?: Orders[]
}

export interface EmptyList {
}

export interface Geo {
  lat?: number
  lng?: number
}

export interface Address {
  city?: string
  zip_code?: string
  geo?: Geo
}

export interface Items {
  sku?: string
  qty?: number
}

export interface Orders {
  order_id?: string
  total?: number
  items?: Items[]
}