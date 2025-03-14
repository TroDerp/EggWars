package me.rosillogames.eggwars.player;

import java.util.List;
import javax.annotation.Nullable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import com.google.common.collect.Lists;
import me.rosillogames.eggwars.menu.EwMenu;

public class MenuPlayer
{
    private final EwPlayer player;
    @Nullable
    private EwMenu menu;
    private int page = -1;
    private final List<EwMenu> parentTree = Lists.newArrayList();
    @Nullable
    private Inventory mcInventory;

    public MenuPlayer(EwPlayer pl)
    {
        this.player = pl;
    }

    public void setCurrentInventory(Inventory mcInv)
    {
        this.mcInventory = mcInv;
    }

    @Nullable
    public Inventory getCurrentInventory()
    {
        return this.mcInventory;
    }

    @Nullable
    public List<EwMenu> getParentTree()
    {
        return this.parentTree;
    }

    public boolean openMenu(EwMenu ewMenu)
    {
        if (ewMenu == null)
        {
            return false;
        }

        boolean addedParent = false;
        int prevPage = this.page;
        this.page = 0;

        if (this.menu == null)
        {
            this.parentTree.clear();
        }
        else if (this.menu.getMenuType() != ewMenu.getMenuType() && !this.parentTree.contains(this.menu))
        {
            this.parentTree.add(0, this.menu);
            addedParent = true;
        }

        boolean flag = ewMenu.addOpener(this);

        if (!flag)
        {
            if (addedParent)
            {
                this.parentTree.remove(0);
            }

            this.page = prevPage;
        }

        return flag;
    }

    public void setMenu(EwMenu ewMenu)
    {
        this.menu = ewMenu;
    }

    public int getCurrentPage()
    {
        return this.page;
    }

    public void setCurrentPage(int idx)
    {
        this.page = idx;
    }

    public void closeMenu()
    {
        this.closeMenu(1);
    }

    /**
     * @param parentTarget
     * If parentTarget >= 1 then go to previous,
     * if == 0 then close completely,
     * if == -1 then go to first opened **/
    public void closeMenu(int parentTarget)
    {
        EwMenu parentMenu = null;

        if (parentTarget != 0 && !this.parentTree.isEmpty())
        {//the higher the target is, the older the entry will be
            parentTarget = parentTarget == -1 ? this.parentTree.size() : parentTarget;
            parentTarget = Math.min(parentTarget, this.parentTree.size()) - 1;
            parentMenu = this.parentTree.get(parentTarget);

            for (int i = 0; i <= parentTarget; ++i)
            {//Remove from the tree all parents, from current to the one we are going back to
                this.parentTree.remove(0);//0 becasue the index of others is always shifting
            }
        }
        else if (!this.parentTree.isEmpty())
        {
            this.parentTree.clear();
        }

        if (parentMenu != null)
        {
            parentMenu.addOpener(this);/* close listener is called when opening another inventory */
            return;
        }
        else
        {
            this.getPlayer().closeInventory();/* removeOpener is called from close listener */
        }
    }

    @Nullable
    public EwMenu getMenu()
    {
        return this.menu;
    }

    public EwPlayer getEwPlayer()
    {
        return this.player;
    }

    public Player getPlayer()
    {
        return this.player.getPlayer();
    }
}
