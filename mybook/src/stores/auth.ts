import { ref } from 'vue';

export const isLoggedIn = ref(!!localStorage.getItem('accessToken'));

export function setLoggedIn(value: boolean) {
  isLoggedIn.value = value;
}
