// src/stores/user.js

import { defineStore } from 'pinia';

export const useUserStore = defineStore('user', {
    state: () => ({
        currentUser: null,   // 用户对象
        currentUserId: null, // 用户 id
    }),
    actions: {
        /**
         * 设置用户数据到 Store
         * @param {Object} user - 包含用户信息的对象，必须有 id 字段
         */
        setUserData(user) {
            this.currentUser = user;
            // 确保 id 与后端返回字段一致
            this.currentUserId = user.id;
            console.log('Pinia: 用户 ID 已设置:', user.id);
        },
        /**
         * 清除 Store 中的用户数据
         */
        clearUserData() {
            this.currentUser = null;
            this.currentUserId = null;
            console.log('Pinia: 用户数据已清除');
        },
    },
});