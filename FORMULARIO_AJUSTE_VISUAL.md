# AJUSTE VISUAL — BANCO DE MINUTAGENS

> Use este formulário somente para alterações visuais. Preencha os campos e anexe imagens quando necessário.

## Escopo

**Modo que será alterado:**

- [ ] Lite
- [ ] Full
- [ ] Ambos

**Tela, guia ou componente:**

`[PREENCHER — exemplo: Início > cards das séries]`

**Tema de cor:**

- [ ] Todos os temas de cor
- [ ] Somente: `[PREENCHER — vermelho, roxo, azul, verde ou laranja]`

**Tipo de aparelho ou tamanho de tela em que o problema aparece:**

- [ ] Todos
- [ ] Celular pequeno
- [ ] Celular grande
- [ ] Outro: `[PREENCHER]`

## Alteração solicitada

**O que deve ser alterado:**

`[PREENCHER]`

**Resultado visual esperado:**

`[PREENCHER — descreva posição, tamanho, cor, espaçamento ou comportamento visual]`

**O que deve permanecer visualmente igual:**

`[PREENCHER]`

**Referência visual:**

- [ ] Imagem anexada
- [ ] Tela atual do aplicativo
- [ ] Preview criado anteriormente
- [ ] Somente a descrição deste formulário

**Deseja receber um preview antes da implementação?**

- [ ] Não, pode implementar diretamente
- [ ] Sim, enviar preview antes de alterar o código

## Restrições obrigatórias

Faça apenas a alteração visual descrita e somente no modo selecionado.

Antes de editar:

1. Localize o componente que realmente renderiza a tela.
2. Confirme se ele pertence ao Lite, ao Full ou se é compartilhado.
3. Verifique onde o componente é chamado antes de alterar sua assinatura.
4. Preserve as cores dinâmicas usando `MaterialTheme.colorScheme.primary` quando o elemento acompanhar o tema escolhido pelo usuário.
5. Se o componente for compartilhado entre Lite e Full, isole a mudança para impedir alterações no modo não selecionado.

Não altere:

- sincronização automática ou manual com o Google Sheets;
- funcionamento offline e fila de sincronização;
- Apps Script;
- importação, exportação JSON ou seus formatos;
- integração com o TMDB;
- armazenamento local, preferências ou dados do usuário;
- modelos de dados e compatibilidade com dados já salvos;
- cálculos de minutagens, duração, progresso ou status;
- preenchimento em lote e cópia de minutagens;
- regras de salvamento automático;
- navegação, gesto de voltar ou comportamento das telas;
- ações, estados habilitados ou funções de botões existentes;
- textos, ícones ou componentes que não foram citados no pedido;
- o modo visual que não foi selecionado.

Não reescreva arquivos completos. Faça a menor alteração possível e não inclua melhorias fora do pedido.

## Validação obrigatória

Depois da alteração:

1. Execute `:app:testDebugUnitTest` quando a mudança alcançar código compartilhado ou comportamento verificável.
2. Execute `:app:assembleDebug`.
3. Execute `:app:lintDebug`.
4. Corrija qualquer erro causado pela alteração antes de concluir.
5. Informe exatamente quais arquivos foram modificados.
6. Resuma o que mudou em cada arquivo.
7. Confirme quais modos e temas de cor foram afetados.
8. Forneça o link do APK de depuração gerado.

## Observações adicionais

`[PREENCHER OU APAGAR ESTE CAMPO]`
