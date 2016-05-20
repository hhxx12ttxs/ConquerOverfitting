/**
 */
package com.tocea.scertify.architecture.xadl.architectureDSL.impl;

import com.tocea.scertify.architecture.xadl.architectureDSL.ArchitectureDSLPackage;
import com.tocea.scertify.architecture.xadl.architectureDSL.Arity;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Arity</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link com.tocea.scertify.architecture.xadl.architectureDSL.impl.ArityImpl#getValue <em>Value</em>}</li>
 *   <li>{@link com.tocea.scertify.architecture.xadl.architectureDSL.impl.ArityImpl#isUnbound <em>Unbound</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ArityImpl extends MinimalEObjectImpl.Container implements Arity
{
  /**
   * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getValue()
   * @generated
   * @ordered
   */
  protected static final int VALUE_EDEFAULT = 0;

  /**
   * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getValue()
   * @generated
   * @ordered
   */
  protected int value = VALUE_EDEFAULT;

  /**
   * The default value of the '{@link #isUnbound() <em>Unbound</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isUnbound()
   * @generated
   * @ordered
   */
  protected static final boolean UNBOUND_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isUnbound() <em>Unbound</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isUnbound()
   * @generated
   * @ordered
   */
  protected boolean unbound = UNBOUND_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ArityImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return ArchitectureDSLPackage.Literals.ARITY;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public int getValue()
  {
    return value;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setValue(int newValue)
  {
    int oldValue = value;
    value = newValue;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ArchitectureDSLPackage.ARITY__VALUE, oldValue, value));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public boolean isUnbound()
  {
    return unbound;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setUnbound(boolean newUnbound)
  {
    boolean oldUnbound = unbound;
    unbound = newUnbound;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, ArchitectureDSLPackage.ARITY__UNBOUND, oldUnbound, unbound));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case ArchitectureDSLPackage.ARITY__VALUE:
        return getValue();
      case ArchitectureDSLPackage.ARITY__UNBOUND:
        return isUnbound();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case ArchitectureDSLPackage.ARITY__VALUE:
        setValue((Integer)newValue);
        return;
      case ArchitectureDSLPackage.ARITY__UNBOUND:
        setUnbound((Boolean)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case ArchitectureDSLPackage.ARITY__VALUE:
        setValue(VALUE_EDEFAULT);
        return;
      case ArchitectureDSLPackage.ARITY__UNBOUND:
        setUnbound(UNBOUND_EDEFAULT);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case ArchitectureDSLPackage.ARITY__VALUE:
        return value != VALUE_EDEFAULT;
      case ArchitectureDSLPackage.ARITY__UNBOUND:
        return unbound != UNBOUND_EDEFAULT;
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    if (eIsProxy()) return super.toString();

    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (value: ");
    result.append(value);
    result.append(", unbound: ");
    result.append(unbound);
    result.append(')');
    return result.toString();
  }

} //ArityImpl

