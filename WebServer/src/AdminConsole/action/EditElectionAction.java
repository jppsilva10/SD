package AdminConsole.action;

import AdminConsole.model.AdminConsoleBean;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.config.entities.Parameterizable;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditElectionAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String titulo;
    private String descricao;
    private String tipo;
    private String dia_i = null;
    private String mes_ i = null;
    private String ano_i = null;
    private String dia_f = null;
    private String mes_ f = null;
    private String ano_f = null;
    private String tituloAnterior;
    private Calendar inicio, fim;

    public void setTitulo(String titulo){
	this.titulo = titulo;
    } 

    public void setDescricao(String descricao){
	this.descricao = descricao;
    }     

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setTituloAnterior(String tituloAnterior){
	this.tituloAnterior = tituloAnterior,
    } 
    
    @Override
    public String execute()
    {
        AdminConsoleBean acb;
        try {
            acb = getAdminConsoleBean();
        } catch (NotBoundException e) {
            return "fail";
        } catch (RemoteException e) {
            return "fail";
        }
	
	inicio = new GregorianCalendar(Integer.parseInt(dia_i), Integer.parseInt(mes_i)-1, Integer.parseInt(ano_i));
	fim = new GregorianCalendar(Integer.parseInt(dia_f), Integer.parseInt(mes_f)-1, Integer.parseInt(ano_f));
	String str = acb.editEleicao(titulo, descricao, inicio, fim, tipo, tituloAnterior)
	
        return "success";
    }

     public String setLists(){
        return "success";
    }

    public ArrayList<String> getDaysList()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(int i=1; i<32; i++){
            list.add("" + i);
        }
        return list;
    }

    public ArrayList<String> getMonthsList()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(int i=1; i<13; i++){
            list.add("" + i);
        }
        return list;
    }

    public ArrayList<String> getYearsList()
    {
        ArrayList<String> list = new ArrayList<String>();
        for(int i=2000; i<2031; i++){
            list.add("" + i);
        }
        return list;
    }

    public AdminConsoleBean getAdminConsoleBean() throws NotBoundException, RemoteException
    {
        if(!session.containsKey("AdminConsoleBean"))
            this.setAdminConsoleBean(new AdminConsoleBean());
        return (AdminConsoleBean) session.get("AdminConsoleBean");
    }

    public void setAdminConsoleBean(AdminConsoleBean acBean) {
        this.session.put("AdminConsoleBean", acBean);
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

}
