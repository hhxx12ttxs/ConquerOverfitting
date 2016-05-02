/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package chamiloda.persistence.repository;

import chamiloda.domain.ErrorController;
import chamiloda.domain.utilities.StringUtilities;
import chamiloda.domain.contentobjects.*;
import chamiloda.domain.utilities.ChamiloUtilities;
import chamiloda.persistence.repository.db.DbTable;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mmeu164
 */
public final class Mapper
{

    private final String TABLES_QUERY = "SELECT name FROM sqlite_master WHERE type='table' AND not name='sqlite_sequence' ORDER BY name";
    
    private Connection connection;

    private ObjectDefinitions objectDefinitions;
    private List<RepositoryCategory> categories;
    private List<ContentObjectImplementation> objects;
    private RepositoryCategoryMapper catMapper;
    private ContentObjectMapper objMapper;

    public Mapper(Connection connection, ObjectDefinitions objectDefinitions)
    {
        this.connection = connection;
        this.catMapper = null;
        this.objMapper = null;
        this.objectDefinitions = objectDefinitions;
        
        // this.readAvailableTables(); // unused at the moment
        this.readData();
    }

    public void readData()
    {
        catMapper = new RepositoryCategoryMapper
                (connection, this.objectDefinitions.getCategoryDef());
        categories = catMapper.getCategories();
        objMapper = new ContentObjectMapper
                (connection, categories, this.objectDefinitions);
        objects = objMapper.readContentObjects();
    }
            
    /**
     * unused for now
     */
    private ArrayList<String> readAvailableTables()
    {
        ArrayList<String> availableTables = new ArrayList<String>();
        try
        {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(TABLES_QUERY);
            while (resultSet.next())
                availableTables.add(resultSet.getString("name"));
            statement.close();
        }
        catch(Exception e)
        {
            ErrorController.getInstance().processError("Data read failed", e, "Types support scan failed", true, true);
        }
        return availableTables;
    }

    public List<RepositoryCategory> getCategories()
    {
        return new ArrayList<RepositoryCategory>(this.categories);
    }

    public RepositoryCategory getCategory(int id)
    {
        if (id == 0)
            return null;
        for (RepositoryCategory cat : categories)
        {
            if (cat.getId() == id)
                return cat;
        }
        throw new IllegalArgumentException("Category does not exist");
    }

    /**
     *
     * @param path      Path with slashes as separators
     * @return          The requested category
     * @throws IllegalArgumentException when the category was not found
     */
    public RepositoryCategory getCategory(String path) throws IllegalArgumentException
    {
        path = ChamiloUtilities.getValidPath(path);

        for (RepositoryCategory cat : categories)
        {
            if (cat.getPathString().equals(path))
                return cat;
        }
        throw new IllegalArgumentException("Category does not exist");
    }

    public List<RepositoryCategory> getChildren(RepositoryCategory category)
    {
        int catId = Category.isRoot(category)? 0 : category.getId();
        List<RepositoryCategory> children = new ArrayList<RepositoryCategory>();
        for (RepositoryCategory cat : categories)
        {
            if (!RepositoryCategory.isRoot(cat))
            {
                RepositoryCategory parent = cat.getParent();
                int parId = Category.isRoot(parent)? 0 : parent.getId();
                if (catId == parId)
                    children.add(cat);
            }
        }
        return children;
    }

    /**
     * Creates categories if the path end point doesn't exist yet; if it does, it throws an exception.
     * @param path  The path string of the categories to make.
     * @return      the created category
     * @throws IllegalArgumentException
     */
    public RepositoryCategory createCategoryIfNotExist(String path)
    {
        return createCategoryFromPath(path, true);
    }

    /**
     * Creates categories if the path end point doesn't exist yet,
     * and returns the final found or created category.
     * @param path  The path string of the categories to make.
     * @return      the created category
     */
    public RepositoryCategory createCategory(String path)
    {
        return createCategoryFromPath(path, false);
    }

    private RepositoryCategory createCategoryFromPath(String path, boolean onlyifnotexists)
    {
        RepositoryCategory cat = null;
        RepositoryCategory lastcat = null;
        RepositoryCategory fetchedcat = null;
        path = ChamiloUtilities.getValidPath(path);
        try
        {
            lastcat = getCategory(path);
            fetchedcat = lastcat;
        }
        catch(IllegalArgumentException iae)
        {
            // trim off first char
            String sep = RepositoryCategory.CATSEPARATOR;
            path = StringUtilities.trim(path, RepositoryCategory.CATSEPARATORCHAR);

            String[] categoryPaths = path.split(sep);
            String fullpath = "";
            for (int i = 0; i < categoryPaths.length; i++)
            {
                fullpath+=sep + categoryPaths[i];
                cat = null;
                try
                {
                    cat = getCategory(fullpath);
                    lastcat=cat;
                }
                catch(IllegalArgumentException iae2)
                {
                    lastcat=makeCategory(categoryPaths[i],lastcat);
                }
            }
        }
        if (fetchedcat !=null && onlyifnotexists)
            throw new IllegalArgumentException("Category already exists");

        return lastcat;
    }

    private RepositoryCategory makeCategory(String name, RepositoryCategory parent)
    {
        List<RepositoryCategory> parentChildren = getChildren(parent);
        int maxDisplayOrder = -1;
        for (int i = 0; i < parentChildren.size(); i++) {
            if (maxDisplayOrder < parentChildren.get(i).getDisplayOrder())
                maxDisplayOrder = parentChildren.get(i).getDisplayOrder();
        }
        maxDisplayOrder++;

        RepositoryCategory cat = new RepositoryCategory
                (-1, name, parent, maxDisplayOrder, this.getUserId());

        // create in db
        if (catMapper.insertCategory(cat));
            categories.add(cat);
        return cat;
    }

    public boolean updateCategory(String path)
    {
        return updateCategory(getCategory(path));
    }

    public boolean updateCategory(int id)
    {
        return updateCategory(getCategory(id));
    }

    public boolean updateCategory(RepositoryCategory category) throws IllegalArgumentException
    {
        int index=getCategoryIndex(category);
        if (index == -1)
            throw new IllegalArgumentException("Can't update Repository");
        else
        {
            sortChildCategories(category.getParent());
            if (catMapper.updateCategory(category))
            {
                categories.set(index, category);
                return true;
            }
        }
        return false;
    }

    public void moveCategoryOrder(RepositoryCategory category, boolean down)
    {
        // reset display order of all siblings
        List<RepositoryCategory> siblings = sortChildCategories(category.getParent());

        int newpos = category.getDisplayOrder() + (down ? 1 : -1);
        if (newpos <0 || newpos >= siblings.size())
            return; // nothing to change
        
        RepositoryCategory switchObject = null;
        for (RepositoryCategory cat : siblings)
        {
            if (cat.getDisplayOrder() == newpos)
            {
                switchObject = cat;
                break;
            }
        }
        switchObject.setDisplayOrder(category.getDisplayOrder());
        category.setDisplayOrder(newpos);
        
        this.updateCategory(switchObject);
        this.updateCategory(category);
    }
    
    public List<RepositoryCategory> sortChildCategories(RepositoryCategory parent)
    {
        List<RepositoryCategory> siblings = getChildren(parent);
        // reset display order of all siblings
        Collections.sort(siblings);
        for (int i = 0; i < siblings.size(); i++) {
            siblings.get(i).setDisplayOrder(i);
        }
        return siblings;
    }
    
    public boolean deleteCategory(String path) throws IllegalArgumentException
    {
        return deleteCategory(getCategory(path));
    }

    public boolean deleteCategory(int id) throws IllegalArgumentException
    {
        return deleteCategory(getCategory(id));
    }

    public boolean deleteCategory(RepositoryCategory category)
    {
        int index=getCategoryIndex(category);
        if (index == -1)
            throw new IllegalArgumentException("Can't delete Repository");
        List<RepositoryCategory> children = getChildren(category);
        // test for child categories
        if (!children.isEmpty())
            throw new IllegalArgumentException("Category has child categories");
        // test for use by content objects
        for (ContentObjectImplementation obj: objects)
        {
            if (obj.getParent() != null && obj.getParent().getId() == category.getId())
                throw new IllegalArgumentException("Category contains content objects");
        }
        if(catMapper.deleteCategory(category))
        {
            categories.remove(index);
            return true;
        }
        return false;
    }

    private int getCategoryIndex(RepositoryCategory category)
    {
        if (RepositoryCategory.isRoot(category))
            return -1;
        int index=-1;
        for (int i = 0; i < categories.size(); i++)
            if (categories.get(i).getId() == category.getId())
            {
                index=i;
                break;
            }
        if (index == -1)
            throw new IllegalArgumentException("ID does not exist in list");
        return index;
    }

    public List<ContentObjectImplementation> getContentObjects()
    {
        return new ArrayList<ContentObjectImplementation>(objects);
    }

    public List<ContentObjectImplementation> getObjectsForCategory(RepositoryCategory parent)
    {
        if (RepositoryCategory.isRoot(parent))
            parent = RepositoryCategory.getRootDummy(0);
        List<ContentObjectImplementation> objs = new ArrayList<ContentObjectImplementation>();
        for (ContentObjectImplementation coi : objects)
        {
            RepositoryCategory coiParent = coi.getParent();
            if (RepositoryCategory.isRoot(coiParent))
                coiParent = RepositoryCategory.getRootDummy(0);
            if (coiParent.getId() == parent.getId())
                objs.add(coi);
        }
        return objs;
    }
    
    public int getObjectsLength()
    {
        return objects.size();
    }

    public ContentObjectImplementation getObject(int index)
    {
        return objects.get(index);
    }

    public ContentObjectImplementation getObjectFromId(int id)
    {
        return getObjectFromId(id, this.objects);
    }
    
    private ContentObjectImplementation getObjectFromId(int id,
            List<ContentObjectImplementation> objs)
    {
        for (ContentObjectImplementation coi : objs)
        {
            if (coi.getId() == id)
                return coi;
        }
        return null;
    }


    private int getObjectIndexFromId(int id,
            List<ContentObjectImplementation> objs)
    {
        for (int i = 0; i < objs.size(); i++)
        {
            ContentObjectImplementation coi = objs.get(i);
            if (coi.getId() == id)
                return i;
        }
        return -1;
    }

    public List<ContentObjectImplementation> getObjectsFromNumber(int objectNumber)
    {
        List<ContentObjectImplementation> objectsList = new ArrayList<ContentObjectImplementation>();
        for (ContentObjectImplementation obj: objects)
        {
            if (obj.getObjectNumber() == objectNumber)
                objectsList.add(obj);
        }
        if (objectsList.isEmpty())
            throw new IllegalArgumentException("Object number does not exist");
        return objectsList;
    }

    public ContentObjectImplementation getObjectFromNumber(int objectNumber)
    {
        // throws an exception if not found
        List<ContentObjectImplementation> objectsList = getObjectsFromNumber(objectNumber);
        java.util.Date maxdate = new Date(0);
        ContentObjectImplementation maxObj = null;
        for (ContentObjectImplementation obj : objectsList)
        {
            java.util.Date curdate = obj.getCreationDate();
            if (curdate.after(maxdate))
            {
                maxdate = curdate;
                maxObj = obj;
            }
        }
        return maxObj;
    }

    public boolean hasAlternates(int objectNumber)
    {
        int found = 0;
        for (ContentObjectImplementation obj: objects)
        {
            if (obj.getObjectNumber() == objectNumber)
            {
                found++;
                if (found > 1)
                    return true;
            }
        }
        if (found == 0)
            throw new IllegalArgumentException("Object number does not exist");
        return false;
    }

    /**
     * Adds a new object to the database
     * @param obj
     * @return 
     */
    public boolean addObject(ContentObjectImplementation obj)
    {
        return addObject(obj,-1);
    }

    /**
     * Adds a new object, as alternate version of an existing one
     * @param obj   The object to add
     * @param refid The ID of the original object
     * @return 
     */
    public boolean addObject(ContentObjectImplementation obj, int refid)
    {
        if (obj.getParent() == null)
            obj.setParent(RepositoryCategory.getRootDummy(0));
        if(refid > 0)
        {
            if(!ObjectNumbersContain(this.objects, refid))
                throw new IllegalArgumentException("Object number does not exist");
            obj.setObjectNumber(refid);
        }
        int objId = objMapper.addObject(obj);
        if (objId >= 0)
        {
            if (refid <= 0)
                obj.setObjectNumber(objId);
            this.objects.add(obj);
            return true;
        }
        return false;
    }

    private int getHighestObjectNumber(List<ContentObjectImplementation> contentObjects)
    {
        int highest=0;
        for (ContentObjectImplementation obj: contentObjects)
        {
            int id = obj.getObjectNumber();
            if (id > highest)
                highest=id;
        }
        return highest;
    }

    private boolean ObjectNumbersContain(List<ContentObjectImplementation> contentObjects, int number)
    {
        for (ContentObjectImplementation obj: contentObjects)
        {
            if (obj.getObjectNumber() == number)
                return true;
        }
        return false;
    }

    public List<Integer> getObjectNumbers(List<ContentObjectImplementation> contentObjects)
    {
        List<Integer> objectNumbers = new ArrayList<Integer>();
        for (ContentObjectImplementation obj: contentObjects)
        {
            int objecNumber = obj.getObjectNumber();
            if (!objectNumbers.contains(objecNumber))
                objectNumbers.add(objecNumber);
        }
        return objectNumbers;
    }
    
    /**
     * Updates an object in the database. If the object was a clone of an
     * existing object, the original object stored in the Mapper will be
     * adjusted to it, and its child relations updated. All children should
     * have already been added for this to work, though.
     * @param obj   The object to update.
     * @return 
     */
    public Boolean updateObject(ContentObjectImplementation obj)
    {
        if (obj.getId() == -1)
            return false;
        
        Boolean success = true;
        ContentObjectImplementation origObj = this.getObjectFromId(obj.getId());
        if (origObj != obj)
        {
            origObj.fillCloneFrom(obj);
            List<ComplexContentObjectImplementation> children = obj.getChildren();
            List<ComplexContentObjectImplementation> origObjChildren = origObj.getChildren();
            if(!(children.isEmpty() && origObjChildren.isEmpty()))
            {
                for(ComplexContentObjectImplementation origChild: origObjChildren)
                {
                    success = origObj.removeChild(objMapper, origChild) && success;
                }
                for(ComplexContentObjectImplementation child: children)
                {
                    success = origObj.addChild(objMapper, child) && success;
                }
                success = origObj.sortChildren(objMapper) && success;
            }
        }
        return objMapper.updateObject(origObj) && success;
    }

    public Boolean deleteObject(ContentObjectImplementation obj)
    {
        int listIndex = getObjectIndexFromId(obj.getId(), this.objects);
        if (listIndex < 0)
            throw new IllegalArgumentException("ID does not exist in list");
        if (!obj.getChildren().isEmpty())
            throw new IllegalArgumentException("Object has linked children");
        if (isLinkedChild(obj))
            throw new IllegalArgumentException("Object is linked to a parent");
        
        if(!objMapper.deleteObject(objects.get(listIndex)))
            return false;
        objects.remove(listIndex);
        return true;
    }

    public Boolean isLinked(ContentObjectImplementation obj)
    {
        return isLinkedChild(obj) || hasLinkedChildren(obj);
    }

    public Boolean isLinkedChild(ContentObjectImplementation obj)
    {
        for (int i = 0; i < objects.size(); i++)
        {
            List<ComplexContentObjectImplementation> children = objects.get(i).getChildren();
            for (int j = 0; j < children.size(); j++)
            {
                if (children.get(j).getEncapsulatedObject().getId() == obj.getId())
                    return true;
            }
        }
        return false;
    }
    
    public boolean hasLinkedChildren(ContentObjectImplementation obj)
    {
        return !obj.getChildren().isEmpty();
    }

    public boolean unlinkObject(ContentObjectImplementation obj)
    {
        boolean success = unlinkChildren(obj);
        if (!unlinkFromOtherObjects(obj))
            success=false;
        return success;
    }
            
    public boolean unlinkChildren(ContentObjectImplementation obj)
    {
        boolean success = true;
        List<ComplexContentObjectImplementation> children = 
            obj.getChildren();
        for (int i = 0; i < children.size(); i++)
        {
            if (!obj.removeChild(objMapper, children.get(i)))
                success =false;
        }
        return success;
    }
    
    public boolean unlinkFromOtherObjects(ContentObjectImplementation obj)
    {
        boolean success = true;
        for (int i = 0; i < objects.size(); i++)
        {
            ContentObjectImplementation parent = objects.get(i);
            List<ComplexContentObjectImplementation> children = objects.get(i).getChildren();
            for (int j = 0; j < children.size(); j++)
            {
                ComplexContentObjectImplementation child = children.get(j);
                if (children.get(j).getEncapsulatedObject().getId() == obj.getId())
                {
                    if (!parent.removeChild(objMapper, child))
                        success =false;
                }
            }
        }
        return success;
    }        
    
    /**
     * Returns a list of all parents the current object is linked to.
     * If the object is linked to a parent twice, the parent will not
     * appear twice in the list.
     * @param obj
     * @return 
     */
    public List<ContentObjectImplementation> getParents(ContentObjectImplementation obj)
    {
        ArrayList<ContentObjectImplementation> parents
                = new ArrayList<ContentObjectImplementation>();
        for (int i = 0; i < objects.size(); i++)
        {
            ContentObjectImplementation parent = objects.get(i);
            List<ComplexContentObjectImplementation> children = parent.getChildren();
            for (int j = 0; j < children.size(); j++)
            {
                if (children.get(j).getEncapsulatedObject().getId() == obj.getId())
                {
                    parents.add(parent);
                    break; // each parent is only added once
                }
            }
        }
        return parents;
    }
    
    /**
     * Returns a list of all parents the current object is linked to.
     * If the object is linked to a parent twice, the parent will not
     * appear twice in the list.
     * @param obj
     * @return 
     */
    public ContentObjectImplementation getParent(ComplexContentObjectImplementation obj)
    {
        ContentObjectImplementation parent;
        int id = obj.getEncapsulatedObject().getId();
        for (int i = 0; i < objects.size(); i++)
        {
            parent = objects.get(i);
            List<ComplexContentObjectImplementation> children = parent.getChildren();
            for (int j = 0; j < children.size(); j++)
            {
                if (children.get(j).getEncapsulatedObject().getId() == id)
                {
                    return parent;
                }
            }
        }
        return null;
    }    
    
    public ComplexContentObjectImplementation addChildToObject
            (ContentObjectImplementation parent,
            ContentObjectImplementation child, Map<String,Object> typeOptions)
    {
        return parent.addChild(objMapper, child, null, typeOptions);
    }
    
    public Boolean addChildToObject(ContentObjectImplementation parent, ComplexContentObjectImplementation child)
    {
        return parent.addChild(objMapper, child);
    }

    public Boolean removeChildFromObject(ContentObjectImplementation parent, ComplexContentObjectImplementation child)
    {
        return parent.removeChild(objMapper, child);
    }
    public Boolean removeChildFromObject(ContentObjectImplementation parent, ContentObjectImplementation child)
    {
        Boolean childfound = false;
        Boolean success = true;
        for (ComplexContentObjectImplementation complexchild : parent.getChildren())
        {
            if (complexchild.getEncapsulatedObject().getId() == child.getId())
            {
                childfound = true;
                if(!parent.removeChild(objMapper, complexchild))
                    success = false;
            }
        }
        return childfound && success;
    }

    public int getUserId()
    {
        // not sure if needs to be implemented
        return 0;
    }

    public ObjectDefinitions getObjectDefinitions()
    {
        return objectDefinitions;
    }
    
    public DbTable getObjectDefinition(String type)
    {
        return objectDefinitions.getSimpleObjectDefinition(type);
    }

    public List<DbTable> getSimpleObjectDefinitions()
    {
        return objectDefinitions.getSimpleObjectDefs();
    }    

    public Map<String, DbTable> getComplexObjectDefinitions()
    {
        return objectDefinitions.getComplexObjectDefs();
    }

}

