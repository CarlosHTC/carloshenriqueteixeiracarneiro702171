import React, { useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import { Card } from "primereact/card";
import { InputText } from "primereact/inputtext";
import { Password } from "primereact/password";
import { Button } from "primereact/button";
import { Message } from "primereact/message";

import { authService } from "../../../services/auth/auth.service";

type LocationState = { from?: { pathname?: string; search?: string } };

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state || {}) as LocationState;

  const redirectTo = useMemo(() => {
    const from = state.from?.pathname ? `${state.from.pathname}${state.from.search ?? ""}` : "/artistas";
    return from;
  }, [state]);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string>("");

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setErrorMsg("");

    if (!username.trim() || !password.trim()) {
      setErrorMsg("Informe usuário e senha.");
      return;
    }

    setLoading(true);
    try {
      await authService.login(username.trim(), password);
      navigate(redirectTo, { replace: true });
    } catch (err: any) {
      setErrorMsg("Falha ao autenticar. Verifique credenciais e tente novamente.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      className="flex align-items-center justify-content-center"
      style={{ minHeight: "100vh", background: "var(--surface-900)" }}
    >
      <Card
        title="Entrar"
        subTitle="Acesse o Music Manager para gerenciar artistas e álbuns."
        style={{ width: "min(420px, 92vw)" }}
      >
        <form onSubmit={onSubmit} className="flex flex-column gap-3">
          {errorMsg ? <Message severity="error" text={errorMsg} /> : null}

          <div className="w-full">
            <label className="block mb-2" style={{ fontWeight: 600 }}>
              Usuário
            </label>
            <InputText
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Digite seu usuário"
              autoComplete="username"
              className="w-full"
            />
          </div>

          <div className="w-full">
            <label className="block mb-2" style={{ fontWeight: 600 }}>
              Senha
            </label>
            <Password
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Digite sua senha"
              toggleMask
              feedback={false}
              className="p-0"
              autoComplete="current-password"
            />
          </div>

          <Button
            type="submit"
            label={loading ? "Entrando..." : "Entrar"}
            icon="pi pi-sign-in"
            loading={loading}
          />

        </form>
      </Card>
    </div>
  );
}
