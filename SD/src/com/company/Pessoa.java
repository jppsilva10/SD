package com.company;

import java.io.Serializable;
import java.util.Calendar;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

public class Pessoa implements Serializable {
    protected String tipo;
    protected String nome;
    protected String username;
    protected String password;
    protected String departamento;
    protected String contacto;
    protected String morada;
    protected String numero_CC;
    protected Calendar validade_CC;
    protected OAuth2AccessToken accessToken;
    protected String id;

    public Pessoa(String tipo, String nome, String username, String password, String departamento, String contacto, String morada,String numero_CC, Calendar validade_CC) {
        this.tipo = tipo;
        this.nome = nome;
        this.username = username;
        this.password = password;
        this.departamento = departamento;
        this.contacto = contacto;
        this.morada = morada;
        this.numero_CC = numero_CC;
        this.validade_CC = validade_CC;
        this.accessToken = null;
        this.id = "";
    }
    public Boolean equals(Pessoa pessoa){
        if(this.numero_CC.equals(pessoa.numero_CC)) return true;
        return false;
    }

    public String toString(){
        String str = "Tipo: " + tipo + "\n";
        str += "Nome: " + nome + "\n";
        str += "Username: " + username + "\n";
        str += "Password: " + password + "\n";
        str += "Departamento: " + departamento + "\n";
        str += "Contacto: " + contacto + "\n";
        str += "Morada: " + morada + "\n";
        str += "Numero do cartao de cidadao: " + numero_CC + "\n";
        str += "Validade do cartão de cidadao: " + validade_CC.get(Calendar.DAY_OF_MONTH) + "/" + (validade_CC.get(Calendar.MONTH)+1) + "/" + validade_CC.get(Calendar.YEAR) + "\n";
        return str;
    }
    public String GetTipo(){
        return tipo;
    }
    public String GetNome(){
        return nome;
    }
    public String GetUsername(){
        return username;
    }
    public String GetPassword(){
        return password;
    }
    public String GetDepartamento(){
        return departamento;
    }
    public String GetContacto(){
        return contacto;
    }
    public String GetMorada(){
        return morada;
    }
    public String GetNumero_CC(){
        return numero_CC;
    }
    public Calendar GetValidade_CC(){
        return validade_CC;
    }
    public OAuth2AccessToken GetAccessToken(){ return accessToken; }
    public String GetId(){
        return id;
    }
    //-----------------------------------------------------------------------
    public void SetNome(String nome){
        this.nome = nome;
    }
    public void SetUsername(String username){
        this.username = username;
    }
    public void SetPassword(String password){
        this.password = password;
    }
    public void SetDepartamento(String departamento){
        this.departamento = departamento;
    }
    public void SetContacto(String contacto){
        this.contacto = contacto;
    }
    public void SetMorada(String morada){
        this.morada = morada;
    }
    public void SetNumero_CC(String numero_cc){
        this.numero_CC = numero_CC;
    }
    public void SetValidade_CC(Calendar validade_CC){
        this.validade_CC = validade_CC;
    }
    public void SetAccessToken(OAuth2AccessToken accessToken){ this.accessToken = accessToken; }
    public void SetId(String id){
        this.id = id;
    }
}
