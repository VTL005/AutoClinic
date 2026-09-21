import axios from 'axios'
import { authStorage } from '../utils/authStorage'

const apiClient = axios.create({
    baseURL:
        import.meta.env.VITE_API_BASE_URL ??
        'http://localhost:8080',

    timeout: 15000,

    headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
    },
})

apiClient.interceptors.request.use((config) => {
    const accessToken = authStorage.getAccessToken()

    if (accessToken) {
        config.headers.Authorization =
            `Bearer ${accessToken}`
    }

    return config
})

apiClient.interceptors.response.use(
    (response) => response,

    (error) => {
        const status = error.response?.status
        const hadAuthorization =
            Boolean(error.config?.headers?.Authorization)

        if (status === 401 && hadAuthorization) {
            authStorage.clearSession()
        }

        return Promise.reject(error)
    },
)

export default apiClient