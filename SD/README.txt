SDK usado: openjdk-16 version 16

Servidor RMI:
Para criar os servidores RMI deve correr o codigo da classe RmiServerImpl
Os dados de configuração do servidor encontram-se no ficheiro config.properties
O endereço do servidor deve ser mudado para o endereço local da maquina onde irá correr

Para mudar o endereço do servidor deve alterar o parametro com chave RmiAddress
Para mudar o porto do servidor deve alterar o parametro com chave RmiPort
Para mudar o intervalo de tempo entre pings do servidor secundario deve alterar o parametro com chave RmiSleepTime

Mesa de voto:
Para criar uma mesa de voto deve correr o codigo da classe MulticastServer
Cada mesa deve ter um id, o qual será pedido no inicio ao executar o código
Os dados de configuração da mesa de voto encontram-se no ficheiro config.properties
Todos os dados referentes à mesa com id x têm o prefixo Mesax

Para mudar o endereço multicast de comunicação com os terminais deve alterar o parametro com sufixo MulticastAddress
Para mudar o endereço multicast de procura dos terminais deve alterar o parametro com sufixo MulticastAddress2
Para mudar o porto deve alterar o parametro com sufixo Port
Para mudar o departamento dev alterar o parametro com sufixo Departamento

Terminal:
Para criar um terminal deve correr o codigo da class MulticastClient
Cada terminal deve ter um id, o qual será pedido no inicio ao executar o código
Os dados de configuração do terminal encontram-se no ficheiro config.properties
Todos os dados referentes ao terminal com id x têm o prefixo Terminalx

Para mudar a mesa à qual o terminal se deve ligar deve alterar o parametro com sufixo mesa