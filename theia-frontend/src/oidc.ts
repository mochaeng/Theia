import { createReactOidc } from 'oidc-spa/react'
import { z } from 'zod'

export const {
  OidcProvider,
  useOidc,
  getOidc,
  withLoginEnforced,
  enforceLogin,
} = createReactOidc(async () => ({
  issuerUri: 'http://localhost:8888/realms/theia',
  clientId: 'theia-frontend',
  homeUrl: import.meta.env.BASE_URL,
  //scopes: ["profile", "email", "api://my-app/access_as_user"],
  extraQueryParams: () => ({
    ui_locales: 'en', // Keycloak login/register page language
    //audience: "https://my-app.my-company.com/api"
  }),
  decodedIdTokenSchema: z.object({
    preferred_username: z.string(),
    name: z.string(),
    //email: z.string().email().optional()
  }),
  debugLogs: true,
}))

export const fetchWithAuth: typeof fetch = async (input, init) => {
  const oidc = await getOidc()

  if (oidc.isUserLoggedIn) {
    const { accessToken } = await oidc.getTokens()

    ;(init ??= {}).headers = {
      ...init.headers,
      Authorization: `Bearer ${accessToken}`,
    }
  }

  return fetch(input, init)
}
