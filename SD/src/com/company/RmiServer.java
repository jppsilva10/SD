package com.company;

import com.github.scribejava.core.model.OAuth2AccessToken;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Calendar;
public interface RmiServer extends Remote{
    void subscribe(RmiClient cliente) throws RemoteException;

    void subscribe(String mesa_id, RmiMesa cliente) throws RemoteException, RemoteException, NotFoundException;

    Pessoa findPessoa(String numero_CC) throws RemoteException;

    Pessoa findPessoaByUsername(String username) throws RemoteException;

    Pessoa findPessoaByName(String nome) throws RemoteException;

    void createPessoa(String tipo, String nome, String username, String password, String departamento, String contacto, String morada, String numero_CC, Calendar validade_CC) throws RemoteException, DataConflictException.DuplicatedNumero_CC, DataConflictException.DuplicatedUsername;

    void editPessoa(String nome, String username, String password, String departamento, String contacto, String morada, String numero_CC, Calendar validade_CC, String numero_CC_anterior) throws RemoteException, NotFoundException.PessoaNF, DataConflictException.DuplicatedNumero_CC, DataConflictException.DuplicatedUsername;

    Eleicao findEleicao(String titulo) throws RemoteException;

    void createEleicao(String titulo, String descricao, Calendar inicio, Calendar fim, String tipo) throws RemoteException, DataConflictException;

    void editEleicao(String titulo, String descricao, Calendar inicio, Calendar fim, String tipo, String titulo_anterior) throws RemoteException, NotFoundException.EleicaoNF, TimeBoundsException.EleicaoAlreadyStarted, DataConflictException;

    String consultarEleicao(String titulo) throws RemoteException, NotFoundException.EleicaoNF;

    String consultarPessoa(String username) throws RemoteException, NotFoundException.PessoaNF;

    String consultarMesa(String id) throws RemoteException, NotFoundException.MesaNF;

    String listarPessoas() throws RemoteException;

    String listarEleicoes() throws RemoteException;

    String listarListas(String titulo) throws RemoteException, NotFoundException.EleicaoNF;

    String listarPessoaPorLista(String titulo, String lista) throws RemoteException, NotFoundException.EleicaoNF, NotFoundException.ListaNF;

    String listarPessoaPorEleicao(String titulo) throws RemoteException, NotFoundException.EleicaoNF;

    String listarMesas() throws RemoteException;

    String listarMesasFuncionais() throws RemoteException;

    String listarMesasPorEleicao(String eleicao) throws RemoteException, NotFoundException.EleicaoNF;

    String listarEleicoesNaoIniciadas() throws RemoteException;

    String listarEleicoesIniciadas() throws RemoteException;

    String ListarEleicoesTerminadas() throws RemoteException;

    String ListarEleicoesPorMesa(String mesa_id) throws RemoteException, NotFoundException.MesaNF;

   boolean login(String username, String password) throws RemoteException, NotFoundException.PessoaNF;

    String facebookLogin() throws RemoteException;

    Boolean getVoto(String numero_CC, String eleicao) throws RemoteException;

    Mesa findMesa(String mesa_id) throws RemoteException;

    void createMesa(String mesa_id, String departamento) throws RemoteException, DataConflictException;

    void addMesa(String eleicao, String mesa_id) throws RemoteException, NotFoundException.MesaNF, NotFoundException.EleicaoNF, DataConflictException;

    void removeMesa(String eleicao, String mesa_id) throws RemoteException, NotFoundException.MesaNF, NotFoundException.EleicaoNF, DataConflictException;

    void addLista(String eleicao, String nome) throws RemoteException, NotFoundException.EleicaoNF, DataConflictException, TimeBoundsException.EleicaoAlreadyStarted;

    void editLista(String eleicao, String nome, String nome_anterior) throws RemoteException, NotFoundException.EleicaoNF, DataConflictException, TimeBoundsException.EleicaoAlreadyStarted, NotFoundException.ListaNF;

    void addPessoa(String eleicao, String lista, String pessoa) throws RemoteException, NotFoundException.EleicaoNF, NotFoundException.ListaNF, DataConflictException, NotFoundException.PessoaNF, TimeBoundsException.EleicaoAlreadyStarted;

    void removePessoa(String eleicao, String lista, String pessoa) throws RemoteException, NotFoundException.EleicaoNF, NotFoundException.ListaNF, TimeBoundsException.EleicaoAlreadyStarted, NotFoundException.PessoaNF;

    void addVoto(String numero_CC, String eleicao, String lista, String mesa_id) throws RemoteException, NotFoundException.PessoaNF, NotFoundException.EleicaoNF, NotFoundException.MesaNF, NotFoundException.ListaNF, DataConflictException, TimeBoundsException.EleicaoAlreadyTerminated, DataConflictException.InvalidType;

    void setAccessToken(String username, OAuth2AccessToken accessToken) throws RemoteException, NotFoundException.PessoaNF;

    OAuth2AccessToken getAccessToken(String username) throws RemoteException, NotFoundException.PessoaNF;

    String ListarUsers() throws RemoteException;

    void SaveDados() throws RemoteException;
}
