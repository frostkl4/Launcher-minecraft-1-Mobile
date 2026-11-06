# 🌙 Noctis Launcher

Launcher de **Minecraft: Java Edition para Android**, com tema noturno, layout pensado para o celular deitado e ícones em pixel art.

> **Versão modificada não oficial** do [Zalith Launcher 2](https://github.com/ZalithLauncher/ZalithLauncher2) (de MovTery e colaboradores), baseada no commit `3ad5a78`.
> Não é afiliada ao Zalith Launcher, à Mojang nem à Microsoft.

## O que muda em relação ao Zalith Launcher 2

- **Tela inicial nova** (paisagem): botão Jogar grande com a instância selecionada, controle de memória RAM em slider, biblioteca com busca e cards de instâncias, painel de conta com prévia 3D da skin.
- **Barra lateral de navegação** no lugar da barra superior (sobra mais altura para o conteúdo).
- **Tema Noctis**: roxo noturno por padrão, céu com lua e montanhas desenhado em vetor (sem imagem pesada na memória).
- **Ícone novo** em pixel art (lua crescente).
- **Capas para contas offline**: arquivo PNG, Ely.by ou MinecraftCapes (servidas ao jogo pelo servidor de skins local + authlib-injector).
- Busca do CurseForge respeita o limite da API (só os primeiros 10.000 resultados de cada busca) e avisa quando falta a chave de API; sem a chave, as abas de download abrem no Modrinth.
- **Perfil de JVM automático**: G1GC com meta de pausa de 50 ms e teto seguro de RAM conforme a memória do celular (tudo registrado no log do jogo, seção "Noctis JVM Profile"). Argumentos de GC próprios do usuário têm prioridade.
- **Launcher libera a memória enquanto o jogo roda**: o jogo fica num processo separado; depois que ele abre, o processo do launcher é encerrado (se não houver download em andamento).
- Prévia 3D da skin na tela inicial limitada a 30 fps para usar menos CPU.
- Não oferece as atualizações do Zalith oficial (é outro app).

O motor do jogo (PojavLauncher/LWJGL/renderizadores), contas, downloads (Modrinth/CurseForge), mods e controles continuam sendo os do Zalith Launcher 2.

## Como compilar (sem PC)

Cada envio para o GitHub compila o APK sozinho:

1. Abra a aba **Actions** do repositório.
2. Entre na execução mais recente de **Build CI**.
3. Quando terminar (✅), baixe o artefato **debug apk (arm64)** no fim da página, descompacte e instale o `.apk`.

Para outras arquiteturas, edite a lista `archs` em `.github/workflows/push_ci.yml`.

### Chaves opcionais (Secrets do repositório)

| Secret | Para quê |
|---|---|
| `OAUTH_CLIENT_ID` | Login com conta Microsoft (ID de um app registrado no Azure) |
| `CURSEFORGE_API_KEY` | Busca e downloads do CurseForge (opcional: dá para colar a chave no próprio app) |

Sem elas o app compila, mas o login Microsoft não funciona. O CurseForge funciona se você colar uma chave em **Configurações → Launcher → Chave de API do CurseForge** (crie uma grátis em console.curseforge.com); a chave fica salva só no aparelho.

## Licença

GPL-3.0, a mesma do projeto original (veja [LICENSE](LICENSE)). Os termos adicionais do Zalith Launcher 2 continuam valendo para quem distribuir esta versão: não usar o nome "ZalithLauncher"/"ZL", indicar que é uma versão modificada não oficial e não remover os avisos de copyright.

Leia também o README original do Zalith Launcher 2: [English](README_ZALITH_EN_US.md) · [简体中文](README_ZALITH_ZH_CN.md) · [繁體中文](README_ZALITH_ZH_TW.md)
