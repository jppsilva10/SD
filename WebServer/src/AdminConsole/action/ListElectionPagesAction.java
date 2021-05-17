package AdminConsole.action;

import AdminConsole.model.AdminConsoleBean;
import AdminConsole.model.UserBean;
import com.company.NotFoundException;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;

public class ListElectionPagesAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String titulo;

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String execute()
    {
        UserBean ub;
        try {
            ub = getUserBean();
        } catch (NotBoundException e) {
            return "fail";
        } catch (RemoteException e) {
            return "fail";
        }
        return "success";
    }

    public String setElectionPage()
    {
        UserBean ub;
        try {
            ub = getUserBean();
        } catch (NotBoundException e) {
            return "fail";
        } catch (RemoteException e) {
            return "fail";
        }
        ub.setTitulo(titulo);
        return "success";
    }

    public UserBean getUserBean() throws NotBoundException, RemoteException {
        if(!session.containsKey("UserBean"))
            this.setUserBean(new UserBean());
        return (UserBean) session.get("UserBean");
    }

    public void setUserBean(UserBean ub)
    {
        this.session.put("UserBean", ub);
    }

    @Override
    public void setSession(Map<String, Object> session)
    {
        this.session = session;
    }

}