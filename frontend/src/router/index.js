import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
    {
        path: '/',
        redirect: '/login'
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import('../views/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('../views/Register.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { requiresAuth: true },
        children: [
            {
                path: '',
                name: 'Market',
                component: () => import('../views/Market.vue')
            },
            {
                path: 'my-orders',
                name: 'MyOrders',
                component: () => import('../views/MyOrders.vue')
            },
            {
                path: 'create-order',
                name: 'CreateOrder',
                component: () => import('../views/CreateOrder.vue')
            },
            {
                path: 'history',
                name: 'History',
                component: () => import('../views/History.vue')
            },
            {
                path: 'trace',
                name: 'Trace',
                component: () => import('../views/Trace.vue')
            },
            {
                path: 'wallet',
                name: 'Wallet',
                component: () => import('../views/Wallet.vue')
            },
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('../views/Profile.vue')
            },
            {
                path: 'admin/users',
                name: 'AdminUsers',
                component: () => import('../views/AdminUsers.vue'),
                meta: { requiresAdmin: true }
            },
            {
                path: 'admin/transactions',
                name: 'AdminTransactions',
                component: () => import('../views/AdminTransactions.vue'),
                meta: { requiresAdmin: true }
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
        next('/login')
    } else if (to.meta.requiresAdmin && userStore.role !== 3) {
        next('/dashboard')
    } else if (!to.meta.requiresAuth && userStore.isLoggedIn && (to.path === '/login' || to.path === '/register')) {
        next('/dashboard')
    } else {
        next()
    }
})

export default router
