package AdminConsole.action;

import AdminConsole.model.AdminConsoleBean;
import AdminConsole.model.UserBean;
import com.company.NotFoundException;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;

public class LoginAction  extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String username;
    private String password;

    public void setUsername(String username){
        this.username = username;
    }

    public void setPassword(String password){
        this.password = password;
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
        try {
            if (ub.validate(username, password)) {
                ub.setUsername(username);
                ub.setPassword(password);
                return "success";
            }
            return "login";
        } catch (NotFoundException.PessoaNF pessoaNF) {
            return "login";
        } catch (RemoteException e) {
            return "fail";
        }
    }

    public String setUser()
    {
        UserBean ub;
        try {
            ub = getUserBean();
        } catch (NotBoundException e) {
            return "fail";
        } catch (RemoteException e) {
            return "fail";
        }

        if(ub.getLogin()){
            return "success";
        }

        return "login";
    }

    public UserBean getUserBean() throws NotBoundException, RemoteException
    {
        if(!session.containsKey("UserBean"))
            this.setUserBean(new UserBean());
        return (UserBean) session.get("UserBean");
    }

    public void setUserBean(UserBean ub) {
        this.session.put("UserBean", ub);
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
