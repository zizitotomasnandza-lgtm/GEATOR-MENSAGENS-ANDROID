GEATOR MENSAGENS ANDROID

OBJETIVO
Aplicativo novo para receber e ler SMS M-Pesa/e-Mola, mostrar histórico local,
guardar pendentes e enviar ao GEATOR BOT com reenvio automático.

FUNCIONA NO MESMO TELEFONE
Endereço:
http://127.0.0.1:8788

FUNCIONA EM OUTRO TELEFONE NA MESMA REDE
Endereço:
http://IP_DO_TELEFONE_DO_BOT:8788

RECURSOS
- Recebe SMS mesmo com o app fechado.
- Lê até 100 SMS recentes da caixa de entrada.
- Mostra cada SMS no histórico local imediatamente.
- Mantém pendentes quando o bot ainda não tem comprovativo aguardando.
- Reenvia pendentes automaticamente.
- Botão manual REENVIAR PENDENTES.
- Não envia SMS; apenas lê comprovativos.
- Evita duplicados pelo hash do remetente, texto e horário.
- Suporta M-Pesa e e-Mola.

COMPILAÇÃO PELO GITHUB
1. Crie repositório público ou privado.
2. Envie esta pasta.
3. Abra Actions.
4. Execute Build APK.
5. Baixe o artifact geator-mensagens-apk.

CONFIGURAÇÃO
1. Inicie o bot no Termux.
2. Abra o aplicativo.
3. Informe URL e token.
4. Guardar configuração.
5. Dar permissões SMS.
6. Remover restrição de bateria.
7. Testar ligação.
8. Tocar em Ler mensagens do telefone.
