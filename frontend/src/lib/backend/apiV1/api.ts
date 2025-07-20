/* eslint-disable */
/* tslint:disable */
// @ts-nocheck
/*
 * ---------------------------------------------------------------
 * ## THIS FILE WAS GENERATED VIA SWAGGER-TYPESCRIPT-API        ##
 * ##                                                           ##
 * ## AUTHOR: acacode                                           ##
 * ## SOURCE: https://github.com/acacode/swagger-typescript-api ##
 * ---------------------------------------------------------------
 */

export interface UpdateCartItemRequest {
  /**
   * @format int32
   * @min 0
   */
  quantity: number;
}

export interface UpdateUserRequest {
  name?: string;
  phone?: string;
  address?: string;
}

export interface UserResponse {
  /** @format int32 */
  id?: number;
  name?: string;
  email?: string;
  phone?: string;
  address?: string;
  role?: 'USER' | 'ADMIN';
}

export interface ProductRequestDto {
  /**
   * @minLength 0
   * @maxLength 100
   */
  name: string;
  imageUrl?: string;
  /**
   * @format int32
   * @min 0
   */
  price: number;
  /**
   * @format int32
   * @min 0
   */
  stock: number;
  /**
   * @minLength 0
   * @maxLength 500
   */
  description?: string;
  /** @format int32 */
  categoryId: number;
}

export interface CategoryResponseDto {
  /** @format int32 */
  id?: number;
  name?: string;
  /** @format int32 */
  parentId?: number;
  children?: any[];
}

export interface ProductResponseDto {
  /** @format int32 */
  id?: number;
  name?: string;
  imageUrl?: string;
  /** @format int32 */
  price?: number;
  /** @format int32 */
  stock?: number;
  description?: string;
  /** @format date-time */
  createdAt?: string;
  /** @format date-time */
  updatedAt?: string;
  category?: CategoryResponseDto;
}

export interface CategoryRequestDto {
  /**
   * @minLength 0
   * @maxLength 50
   */
  name: string;
  /** @format int32 */
  parentId?: number;
}

export interface OrderPayDTO {
  paymentMethod?: string;
  paymentDetails?: string;
}

export interface OrderDetailDTO {
  /** @format int32 */
  orderId?: number;
  shippingAddress?: string;
  /** @format int32 */
  totalPrice?: number;
  status?:
    | '결제대기'
    | '결제완료'
    | '배송준비중'
    | '배송중'
    | '배송완료'
    | '취소'
    | '환불';
  /** @format date-time */
  orderDate?: string;
  username?: string;
  items?: OrderItemDetailDTO[];
}

export interface OrderItemDetailDTO {
  productName?: string;
  /** @format int32 */
  quantity?: number;
  /** @format int32 */
  unitPrice?: number;
}

export interface OrderItemRequestDTO {
  /** @format int32 */
  productId?: number;
  /** @format int32 */
  quantity?: number;
  /** @format int32 */
  unitPrice?: number;
}

export interface OrderRequestDTO {
  /** @format int32 */
  userId?: number;
  /** @format int32 */
  deliveryId?: number;
  shippingAddress?: string;
  items?: OrderItemRequestDTO[];
}

export interface AddCartItemRequest {
  /** @format int32 */
  productId: number;
  /**
   * @format int32
   * @min 1
   */
  quantity?: number;
}

export interface SignupRequest {
  /** @minLength 1 */
  email: string;
  /**
   * @minLength 8
   * @maxLength 20
   */
  password: string;
  /** @minLength 1 */
  name: string;
}

export interface LoginRequest {
  /** @minLength 1 */
  email: string;
  /** @minLength 1 */
  password: string;
}

export interface ChangePasswordRequest {
  currentPassword?: string;
  newPassword?: string;
}

export interface OrderStatusUpdateDTO {
  /** @format int32 */
  orderId?: number;
  newStatus?:
    | '결제대기'
    | '결제완료'
    | '배송준비중'
    | '배송중'
    | '배송완료'
    | '취소'
    | '환불';
}

export interface OrderListDTO {
  /** @format int32 */
  orderId?: number;
  username?: string;
  /** @format date-time */
  orderDate?: string;
  /** @format int32 */
  totalPrice?: number;
  status?:
    | '결제대기'
    | '결제완료'
    | '배송준비중'
    | '배송중'
    | '배송완료'
    | '취소'
    | '환불';
}

export interface CartDto {
  /** @format int32 */
  cartId?: number;
  /** @format int32 */
  totalQuantity?: number;
  /** @format int32 */
  totalPrice?: number;
  items?: CartItemDto[];
}

export interface CartItemDto {
  /** @format int32 */
  id?: number;
  /** @format int32 */
  productId?: number;
  productName?: string;
  /** @format int32 */
  quantity?: number;
  /** @format int32 */
  unitPrice?: number;
}

export interface Pageable {
  /**
   * @format int32
   * @min 0
   */
  page?: number;
  /**
   * @format int32
   * @min 1
   */
  size?: number;
  sort?: string[];
}

export interface PageResponseDtoUserResponse {
  content?: UserResponse[];
  /** @format int32 */
  pageNumber?: number;
  /** @format int32 */
  pageSize?: number;
  /** @format int64 */
  totalElements?: number;
  /** @format int32 */
  totalPages?: number;
  isLast?: boolean;
}

export interface SalesStatisticsResponseDto {
  /** @format date */
  date?: string;
  /** @format int64 */
  totalSalesAmount?: number;
}

export interface ProductSalesStatisticsResponseDto {
  /** @format int32 */
  productId?: number;
  productName?: string;
  /** @format int64 */
  totalQuantitySold?: number;
  /** @format int64 */
  totalSalesAmount?: number;
}

import type {
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
  HeadersDefaults,
  ResponseType,
} from 'axios';
import axios from 'axios';

export type QueryParamsType = Record<string | number, any>;

export interface FullRequestParams
  extends Omit<AxiosRequestConfig, 'data' | 'params' | 'url' | 'responseType'> {
  /** set parameter to `true` for call `securityWorker` for this request */
  secure?: boolean;
  /** request path */
  path: string;
  /** content type of request body */
  type?: ContentType;
  /** query params */
  query?: QueryParamsType;
  /** format of response (i.e. response.json() -> format: "json") */
  format?: ResponseType;
  /** request body */
  body?: unknown;
}

export type RequestParams = Omit<
  FullRequestParams,
  'body' | 'method' | 'query' | 'path'
>;

export interface ApiConfig<SecurityDataType = unknown>
  extends Omit<AxiosRequestConfig, 'data' | 'cancelToken'> {
  securityWorker?: (
    securityData: SecurityDataType | null
  ) => Promise<AxiosRequestConfig | void> | AxiosRequestConfig | void;
  secure?: boolean;
  format?: ResponseType;
}

export enum ContentType {
  Json = 'application/json',
  JsonApi = 'application/vnd.api+json',
  FormData = 'multipart/form-data',
  UrlEncoded = 'application/x-www-form-urlencoded',
  Text = 'text/plain',
}

export class HttpClient<SecurityDataType = unknown> {
  public instance: AxiosInstance;
  private securityData: SecurityDataType | null = null;
  private securityWorker?: ApiConfig<SecurityDataType>['securityWorker'];
  private secure?: boolean;
  private format?: ResponseType;

  constructor({
    securityWorker,
    secure,
    format,
    ...axiosConfig
  }: ApiConfig<SecurityDataType> = {}) {
    this.instance = axios.create({
      ...axiosConfig,
      baseURL: axiosConfig.baseURL || 'http://localhost:8080',
    });
    this.secure = secure;
    this.format = format;
    this.securityWorker = securityWorker;
  }

  public setSecurityData = (data: SecurityDataType | null) => {
    this.securityData = data;
  };

  protected mergeRequestParams(
    params1: AxiosRequestConfig,
    params2?: AxiosRequestConfig
  ): AxiosRequestConfig {
    const method = params1.method || (params2 && params2.method);

    return {
      ...this.instance.defaults,
      ...params1,
      ...(params2 || {}),
      headers: {
        ...((method &&
          this.instance.defaults.headers[
            method.toLowerCase() as keyof HeadersDefaults
          ]) ||
          {}),
        ...(params1.headers || {}),
        ...((params2 && params2.headers) || {}),
      },
    };
  }

  protected stringifyFormItem(formItem: unknown) {
    if (typeof formItem === 'object' && formItem !== null) {
      return JSON.stringify(formItem);
    } else {
      return `${formItem}`;
    }
  }

  protected createFormData(input: Record<string, unknown>): FormData {
    if (input instanceof FormData) {
      return input;
    }
    return Object.keys(input || {}).reduce((formData, key) => {
      const property = input[key];
      const propertyContent: any[] =
        property instanceof Array ? property : [property];

      for (const formItem of propertyContent) {
        const isFileType = formItem instanceof Blob || formItem instanceof File;
        formData.append(
          key,
          isFileType ? formItem : this.stringifyFormItem(formItem)
        );
      }

      return formData;
    }, new FormData());
  }

  public request = async <T = any, _E = any>({
    secure,
    path,
    type,
    query,
    format,
    body,
    ...params
  }: FullRequestParams): Promise<AxiosResponse<T>> => {
    const secureParams =
      ((typeof secure === 'boolean' ? secure : this.secure) &&
        this.securityWorker &&
        (await this.securityWorker(this.securityData))) ||
      {};
    const requestParams = this.mergeRequestParams(params, secureParams);
    const responseFormat = format || this.format || undefined;

    if (
      type === ContentType.FormData &&
      body &&
      body !== null &&
      typeof body === 'object'
    ) {
      body = this.createFormData(body as Record<string, unknown>);
    }

    if (
      type === ContentType.Text &&
      body &&
      body !== null &&
      typeof body !== 'string'
    ) {
      body = JSON.stringify(body);
    }

    return this.instance.request({
      ...requestParams,
      headers: {
        ...(requestParams.headers || {}),
        ...(type ? { 'Content-Type': type } : {}),
      },
      params: query,
      responseType: responseFormat,
      data: body,
      url: path,
    });
  };
}

/**
 * @title API 서버
 * @version beta
 * @baseUrl http://localhost:8080
 *
 * 팀5 1차 프로젝트 API 서버 문서입니다.
 */
export class Api<
  SecurityDataType extends unknown,
> extends HttpClient<SecurityDataType> {
  api = {
    /**
     * No description
     *
     * @tags cart-controller
     * @name UpdateItemQuantity
     * @summary 장바구니 항목 수량 수정
     * @request PUT:/api/v1/carts/items/{cartItemId}
     */
    updateItemQuantity: (
      cartItemId: number,
      data: UpdateCartItemRequest,
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/v1/carts/items/${cartItemId}`,
        method: 'PUT',
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags cart-controller
     * @name DeleteItem
     * @summary 장바구니 항목 삭제
     * @request DELETE:/api/v1/carts/items/{cartItemId}
     */
    deleteItem: (cartItemId: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/v1/carts/items/${cartItemId}`,
        method: 'DELETE',
        ...params,
      }),

    /**
     * @description 특정 사용자 ID를 통해 상세 정보를 조회합니다.
     *
     * @tags admin-controller
     * @name GetUserById
     * @summary 관리자 - 특정 사용자 정보 조회
     * @request GET:/api/v1/admin/users/{userId}
     */
    getUserById: (userId: number, params: RequestParams = {}) =>
      this.request<UserResponse, any>({
        path: `/api/v1/admin/users/${userId}`,
        method: 'GET',
        format: 'json',
        ...params,
      }),

    /**
     * @description 특정 사용자 ID의 정보를 수정합니다. 이름, 주소, 전화번호를 수정할 수 있습니다.
     *
     * @tags admin-controller
     * @name UpdateUser
     * @summary 관리자 - 특정 사용자 정보 수정
     * @request PUT:/api/v1/admin/users/{userId}
     */
    updateUser: (
      userId: number,
      data: UpdateUserRequest,
      params: RequestParams = {}
    ) =>
      this.request<UserResponse, any>({
        path: `/api/v1/admin/users/${userId}`,
        method: 'PUT',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags product-controller
     * @name UpdateProduct
     * @request PUT:/api/v1/admin/products/{id}
     */
    updateProduct: (
      id: number,
      data: ProductRequestDto,
      params: RequestParams = {}
    ) =>
      this.request<ProductResponseDto, any>({
        path: `/api/v1/admin/products/${id}`,
        method: 'PUT',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags product-controller
     * @name DeleteProduct
     * @request DELETE:/api/v1/admin/products/{id}
     */
    deleteProduct: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/v1/admin/products/${id}`,
        method: 'DELETE',
        ...params,
      }),

    /**
     * No description
     *
     * @tags category-controller
     * @name UpdateCategory
     * @request PUT:/api/v1/admin/categories/{id}
     */
    updateCategory: (
      id: number,
      data: CategoryRequestDto,
      params: RequestParams = {}
    ) =>
      this.request<CategoryResponseDto, any>({
        path: `/api/v1/admin/categories/${id}`,
        method: 'PUT',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags category-controller
     * @name DeleteCategory
     * @request DELETE:/api/v1/admin/categories/{id}
     */
    deleteCategory: (id: number, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/v1/admin/categories/${id}`,
        method: 'DELETE',
        ...params,
      }),

    /**
     * No description
     *
     * @tags order-controller
     * @name PayForOrder
     * @request POST:/api/v1/orders/{orderId}/pay
     */
    payForOrder: (
      orderId: number,
      data: OrderPayDTO,
      params: RequestParams = {}
    ) =>
      this.request<OrderDetailDTO, any>({
        path: `/api/v1/orders/${orderId}/pay`,
        method: 'POST',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags order-controller
     * @name CreateOrder
     * @request POST:/api/v1/orders/orders
     */
    createOrder: (data: OrderRequestDTO, params: RequestParams = {}) =>
      this.request<OrderDetailDTO, any>({
        path: `/api/v1/orders/orders`,
        method: 'POST',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags cart-controller
     * @name AddItem
     * @summary 장바구니에 상품 추가
     * @request POST:/api/v1/carts/items
     */
    addItem: (
      query: {
        /**
         * @format int32
         * @exclusiveMin 0
         */
        userId: number;
      },
      data: AddCartItemRequest,
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/v1/carts/items`,
        method: 'POST',
        query: query,
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name Signup
     * @request POST:/api/v1/auth/signup
     */
    signup: (data: SignupRequest, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/v1/auth/signup`,
        method: 'POST',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name Reissue
     * @request POST:/api/v1/auth/reissue
     */
    reissue: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/v1/auth/reissue`,
        method: 'POST',
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name Logout
     * @request POST:/api/v1/auth/logout
     */
    logout: (params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/v1/auth/logout`,
        method: 'POST',
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name Login
     * @request POST:/api/v1/auth/login
     */
    login: (data: LoginRequest, params: RequestParams = {}) =>
      this.request<void, any>({
        path: `/api/v1/auth/login`,
        method: 'POST',
        body: data,
        type: ContentType.Json,
        ...params,
      }),

    /**
     * No description
     *
     * @tags auth-controller
     * @name ChangePassword
     * @request POST:/api/v1/auth/change-password
     */
    changePassword: (data: ChangePasswordRequest, params: RequestParams = {}) =>
      this.request<string, any>({
        path: `/api/v1/auth/change-password`,
        method: 'POST',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags product-controller
     * @name CreateProduct
     * @request POST:/api/v1/admin/products
     */
    createProduct: (data: ProductRequestDto, params: RequestParams = {}) =>
      this.request<ProductResponseDto, any>({
        path: `/api/v1/admin/products`,
        method: 'POST',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags category-controller
     * @name CreateCategory
     * @request POST:/api/v1/admin/categories
     */
    createCategory: (data: CategoryRequestDto, params: RequestParams = {}) =>
      this.request<CategoryResponseDto, any>({
        path: `/api/v1/admin/categories`,
        method: 'POST',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags user-controller
     * @name GetMe
     * @request GET:/api/v1/users/me
     */
    getMe: (params: RequestParams = {}) =>
      this.request<UserResponse, any>({
        path: `/api/v1/users/me`,
        method: 'GET',
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags user-controller
     * @name UpdateMe
     * @request PATCH:/api/v1/users/me
     */
    updateMe: (data: UpdateUserRequest, params: RequestParams = {}) =>
      this.request<UserResponse, any>({
        path: `/api/v1/users/me`,
        method: 'PATCH',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags order-controller
     * @name UpdateOrderStatus
     * @request PATCH:/api/v1/orders/admin/{orderId}/status
     */
    updateOrderStatus: (
      orderId: number,
      data: OrderStatusUpdateDTO,
      params: RequestParams = {}
    ) =>
      this.request<OrderDetailDTO, any>({
        path: `/api/v1/orders/admin/${orderId}/status`,
        method: 'PATCH',
        body: data,
        type: ContentType.Json,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags product-controller
     * @name GetAllProducts
     * @request GET:/api/v1/products
     */
    getAllProducts: (params: RequestParams = {}) =>
      this.request<ProductResponseDto[], any>({
        path: `/api/v1/products`,
        method: 'GET',
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags product-controller
     * @name GetProductById
     * @request GET:/api/v1/products/{id}
     */
    getProductById: (id: number, params: RequestParams = {}) =>
      this.request<ProductResponseDto, any>({
        path: `/api/v1/products/${id}`,
        method: 'GET',
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags order-controller
     * @name GetMyOrders
     * @request GET:/api/v1/orders
     */
    getMyOrders: (params: RequestParams = {}) =>
      this.request<OrderListDTO[], any>({
        path: `/api/v1/orders`,
        method: 'GET',
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags order-controller
     * @name GetOrderDetail
     * @request GET:/api/v1/orders/{orderId}
     */
    getOrderDetail: (orderId: number, params: RequestParams = {}) =>
      this.request<OrderDetailDTO, any>({
        path: `/api/v1/orders/${orderId}`,
        method: 'GET',
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags order-controller
     * @name GetAllOrders
     * @request GET:/api/v1/orders/admin/all
     */
    getAllOrders: (params: RequestParams = {}) =>
      this.request<OrderListDTO[], any>({
        path: `/api/v1/orders/admin/all`,
        method: 'GET',
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags category-controller
     * @name GetAllCategories
     * @request GET:/api/v1/categories
     */
    getAllCategories: (params: RequestParams = {}) =>
      this.request<CategoryResponseDto[], any>({
        path: `/api/v1/categories`,
        method: 'GET',
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags category-controller
     * @name GetCategoryById
     * @request GET:/api/v1/categories/{id}
     */
    getCategoryById: (id: number, params: RequestParams = {}) =>
      this.request<CategoryResponseDto, any>({
        path: `/api/v1/categories/${id}`,
        method: 'GET',
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags category-controller
     * @name GetRootCategories
     * @request GET:/api/v1/categories/roots
     */
    getRootCategories: (params: RequestParams = {}) =>
      this.request<CategoryResponseDto[], any>({
        path: `/api/v1/categories/roots`,
        method: 'GET',
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags cart-controller
     * @name GetCart
     * @summary 장바구니 조회
     * @request GET:/api/v1/carts
     */
    getCart: (
      query: {
        /**
         * @format int32
         * @exclusiveMin 0
         */
        userId: number;
      },
      params: RequestParams = {}
    ) =>
      this.request<CartDto, any>({
        path: `/api/v1/carts`,
        method: 'GET',
        query: query,
        format: 'json',
        ...params,
      }),

    /**
     * No description
     *
     * @tags cart-controller
     * @name ClearCart
     * @summary 장바구니 비우기
     * @request DELETE:/api/v1/carts
     */
    clearCart: (
      query: {
        /**
         * @format int32
         * @exclusiveMin 0
         */
        userId: number;
      },
      params: RequestParams = {}
    ) =>
      this.request<void, any>({
        path: `/api/v1/carts`,
        method: 'DELETE',
        query: query,
        ...params,
      }),

    /**
     * @description 모든 사용자 목록을 조회합니다. 검색어(search)를 통해 이메일 또는 이름으로 필터링할 수 있습니다.
     *
     * @tags admin-controller
     * @name GetAllUsers
     * @summary 관리자 - 사용자 목록 조회 (페이지네이션, 검색 가능)
     * @request GET:/api/v1/admin/users
     */
    getAllUsers: (
      query: {
        pageable: Pageable;
        search?: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<PageResponseDtoUserResponse, any>({
        path: `/api/v1/admin/users`,
        method: 'GET',
        query: query,
        format: 'json',
        ...params,
      }),

    /**
     * @description 지정된 기간 동안의 일별 또는 월별 총 판매액 통계를 조회합니다. (주의: 대량 데이터 시 비효율적)
     *
     * @tags admin-controller
     * @name GetSalesStatistics
     * @summary 관리자 - 일별/월별 판매액 통계
     * @request GET:/api/v1/admin/statistics/sales
     */
    getSalesStatistics: (
      query: {
        /** @format date */
        startDate: string;
        /** @format date */
        endDate: string;
      },
      params: RequestParams = {}
    ) =>
      this.request<SalesStatisticsResponseDto[], any>({
        path: `/api/v1/admin/statistics/sales`,
        method: 'GET',
        query: query,
        format: 'json',
        ...params,
      }),

    /**
     * @description 상품별 총 판매량 및 총 판매액 통계를 조회합니다. (주의: N+1 쿼리 발생 가능)
     *
     * @tags admin-controller
     * @name GetProductSalesStatistics
     * @summary 관리자 - 상품별 판매량 통계
     * @request GET:/api/v1/admin/statistics/products
     */
    getProductSalesStatistics: (params: RequestParams = {}) =>
      this.request<ProductSalesStatisticsResponseDto[], any>({
        path: `/api/v1/admin/statistics/products`,
        method: 'GET',
        format: 'json',
        ...params,
      }),
  };
}
