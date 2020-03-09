/**
 * ******************************************************************************
 * Copyright (c) 2018 Kiel University and others.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *  *
 * SPDX-License-Identifier: EPL-2.0
 *  ******************************************************************************
 */
package org.eclipse.elk.core.debug.grandom.gRandom.impl;

import org.eclipse.elk.core.debug.grandom.gRandom.Configuration;
import org.eclipse.elk.core.debug.grandom.gRandom.DoubleQuantity;
import org.eclipse.elk.core.debug.grandom.gRandom.Edges;
import org.eclipse.elk.core.debug.grandom.gRandom.Form;
import org.eclipse.elk.core.debug.grandom.gRandom.Formats;
import org.eclipse.elk.core.debug.grandom.gRandom.GRandomPackage;
import org.eclipse.elk.core.debug.grandom.gRandom.Hierarchy;
import org.eclipse.elk.core.debug.grandom.gRandom.Nodes;
import org.eclipse.elk.core.debug.grandom.gRandom.Size;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Configuration</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getSamples <em>Samples</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getForm <em>Form</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getNodes <em>Nodes</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getEdges <em>Edges</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#isMW <em>MW</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getMaxWidth <em>Max Width</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#isMD <em>MD</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getMaxDegree <em>Max Degree</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#isPF <em>PF</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getFraction <em>Fraction</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#isBN <em>BN</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getBigNodes <em>Big Nodes</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#isBNS <em>BNS</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getBigNodeSize <em>Big Node Size</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#isPrio <em>Prio</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getHierarchy <em>Hierarchy</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getSeed <em>Seed</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getFormat <em>Format</em>}</li>
 *   <li>{@link org.eclipse.elk.core.debug.grandom.gRandom.impl.ConfigurationImpl#getFilename <em>Filename</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ConfigurationImpl extends MinimalEObjectImpl.Container implements Configuration
{
  /**
   * The default value of the '{@link #getSamples() <em>Samples</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSamples()
   * @generated
   * @ordered
   */
  protected static final int SAMPLES_EDEFAULT = 0;

  /**
   * The cached value of the '{@link #getSamples() <em>Samples</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSamples()
   * @generated
   * @ordered
   */
  protected int samples = SAMPLES_EDEFAULT;

  /**
   * The default value of the '{@link #getForm() <em>Form</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getForm()
   * @generated
   * @ordered
   */
  protected static final Form FORM_EDEFAULT = Form.TREES;

  /**
   * The cached value of the '{@link #getForm() <em>Form</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getForm()
   * @generated
   * @ordered
   */
  protected Form form = FORM_EDEFAULT;

  /**
   * The cached value of the '{@link #getNodes() <em>Nodes</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getNodes()
   * @generated
   * @ordered
   */
  protected Nodes nodes;

  /**
   * The cached value of the '{@link #getEdges() <em>Edges</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getEdges()
   * @generated
   * @ordered
   */
  protected Edges edges;

  /**
   * The default value of the '{@link #isMW() <em>MW</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isMW()
   * @generated
   * @ordered
   */
  protected static final boolean MW_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isMW() <em>MW</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isMW()
   * @generated
   * @ordered
   */
  protected boolean mW = MW_EDEFAULT;

  /**
   * The default value of the '{@link #getMaxWidth() <em>Max Width</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMaxWidth()
   * @generated
   * @ordered
   */
  protected static final Integer MAX_WIDTH_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getMaxWidth() <em>Max Width</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMaxWidth()
   * @generated
   * @ordered
   */
  protected Integer maxWidth = MAX_WIDTH_EDEFAULT;

  /**
   * The default value of the '{@link #isMD() <em>MD</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isMD()
   * @generated
   * @ordered
   */
  protected static final boolean MD_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isMD() <em>MD</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isMD()
   * @generated
   * @ordered
   */
  protected boolean mD = MD_EDEFAULT;

  /**
   * The default value of the '{@link #getMaxDegree() <em>Max Degree</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMaxDegree()
   * @generated
   * @ordered
   */
  protected static final Integer MAX_DEGREE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getMaxDegree() <em>Max Degree</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getMaxDegree()
   * @generated
   * @ordered
   */
  protected Integer maxDegree = MAX_DEGREE_EDEFAULT;

  /**
   * The default value of the '{@link #isPF() <em>PF</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isPF()
   * @generated
   * @ordered
   */
  protected static final boolean PF_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isPF() <em>PF</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isPF()
   * @generated
   * @ordered
   */
  protected boolean pF = PF_EDEFAULT;

  /**
   * The cached value of the '{@link #getFraction() <em>Fraction</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFraction()
   * @generated
   * @ordered
   */
  protected DoubleQuantity fraction;

  /**
   * The default value of the '{@link #isBN() <em>BN</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isBN()
   * @generated
   * @ordered
   */
  protected static final boolean BN_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isBN() <em>BN</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isBN()
   * @generated
   * @ordered
   */
  protected boolean bN = BN_EDEFAULT;

  /**
   * The cached value of the '{@link #getBigNodes() <em>Big Nodes</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getBigNodes()
   * @generated
   * @ordered
   */
  protected DoubleQuantity bigNodes;

  /**
   * The default value of the '{@link #isBNS() <em>BNS</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isBNS()
   * @generated
   * @ordered
   */
  protected static final boolean BNS_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isBNS() <em>BNS</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isBNS()
   * @generated
   * @ordered
   */
  protected boolean bNS = BNS_EDEFAULT;

  /**
   * The cached value of the '{@link #getBigNodeSize() <em>Big Node Size</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getBigNodeSize()
   * @generated
   * @ordered
   */
  protected Size bigNodeSize;

  /**
   * The default value of the '{@link #isPrio() <em>Prio</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isPrio()
   * @generated
   * @ordered
   */
  protected static final boolean PRIO_EDEFAULT = false;

  /**
   * The cached value of the '{@link #isPrio() <em>Prio</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #isPrio()
   * @generated
   * @ordered
   */
  protected boolean prio = PRIO_EDEFAULT;

  /**
   * The cached value of the '{@link #getHierarchy() <em>Hierarchy</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getHierarchy()
   * @generated
   * @ordered
   */
  protected Hierarchy hierarchy;

  /**
   * The default value of the '{@link #getSeed() <em>Seed</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSeed()
   * @generated
   * @ordered
   */
  protected static final Integer SEED_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getSeed() <em>Seed</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSeed()
   * @generated
   * @ordered
   */
  protected Integer seed = SEED_EDEFAULT;

  /**
   * The default value of the '{@link #getFormat() <em>Format</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFormat()
   * @generated
   * @ordered
   */
  protected static final Formats FORMAT_EDEFAULT = Formats.ELKT;

  /**
   * The cached value of the '{@link #getFormat() <em>Format</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFormat()
   * @generated
   * @ordered
   */
  protected Formats format = FORMAT_EDEFAULT;

  /**
   * The default value of the '{@link #getFilename() <em>Filename</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFilename()
   * @generated
   * @ordered
   */
  protected static final String FILENAME_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getFilename() <em>Filename</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFilename()
   * @generated
   * @ordered
   */
  protected String filename = FILENAME_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ConfigurationImpl()
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
    return GRandomPackage.Literals.CONFIGURATION;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public int getSamples()
  {
    return samples;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setSamples(int newSamples)
  {
    int oldSamples = samples;
    samples = newSamples;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__SAMPLES, oldSamples, samples));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Form getForm()
  {
    return form;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setForm(Form newForm)
  {
    Form oldForm = form;
    form = newForm == null ? FORM_EDEFAULT : newForm;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__FORM, oldForm, form));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Nodes getNodes()
  {
    return nodes;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetNodes(Nodes newNodes, NotificationChain msgs)
  {
    Nodes oldNodes = nodes;
    nodes = newNodes;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__NODES, oldNodes, newNodes);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setNodes(Nodes newNodes)
  {
    if (newNodes != nodes)
    {
      NotificationChain msgs = null;
      if (nodes != null)
        msgs = ((InternalEObject)nodes).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__NODES, null, msgs);
      if (newNodes != null)
        msgs = ((InternalEObject)newNodes).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__NODES, null, msgs);
      msgs = basicSetNodes(newNodes, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__NODES, newNodes, newNodes));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Edges getEdges()
  {
    return edges;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetEdges(Edges newEdges, NotificationChain msgs)
  {
    Edges oldEdges = edges;
    edges = newEdges;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__EDGES, oldEdges, newEdges);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setEdges(Edges newEdges)
  {
    if (newEdges != edges)
    {
      NotificationChain msgs = null;
      if (edges != null)
        msgs = ((InternalEObject)edges).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__EDGES, null, msgs);
      if (newEdges != null)
        msgs = ((InternalEObject)newEdges).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__EDGES, null, msgs);
      msgs = basicSetEdges(newEdges, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__EDGES, newEdges, newEdges));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean isMW()
  {
    return mW;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setMW(boolean newMW)
  {
    boolean oldMW = mW;
    mW = newMW;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__MW, oldMW, mW));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Integer getMaxWidth()
  {
    return maxWidth;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setMaxWidth(Integer newMaxWidth)
  {
    Integer oldMaxWidth = maxWidth;
    maxWidth = newMaxWidth;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__MAX_WIDTH, oldMaxWidth, maxWidth));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean isMD()
  {
    return mD;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setMD(boolean newMD)
  {
    boolean oldMD = mD;
    mD = newMD;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__MD, oldMD, mD));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Integer getMaxDegree()
  {
    return maxDegree;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setMaxDegree(Integer newMaxDegree)
  {
    Integer oldMaxDegree = maxDegree;
    maxDegree = newMaxDegree;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__MAX_DEGREE, oldMaxDegree, maxDegree));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean isPF()
  {
    return pF;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setPF(boolean newPF)
  {
    boolean oldPF = pF;
    pF = newPF;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__PF, oldPF, pF));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public DoubleQuantity getFraction()
  {
    return fraction;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetFraction(DoubleQuantity newFraction, NotificationChain msgs)
  {
    DoubleQuantity oldFraction = fraction;
    fraction = newFraction;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__FRACTION, oldFraction, newFraction);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setFraction(DoubleQuantity newFraction)
  {
    if (newFraction != fraction)
    {
      NotificationChain msgs = null;
      if (fraction != null)
        msgs = ((InternalEObject)fraction).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__FRACTION, null, msgs);
      if (newFraction != null)
        msgs = ((InternalEObject)newFraction).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__FRACTION, null, msgs);
      msgs = basicSetFraction(newFraction, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__FRACTION, newFraction, newFraction));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean isBN()
  {
    return bN;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setBN(boolean newBN)
  {
    boolean oldBN = bN;
    bN = newBN;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__BN, oldBN, bN));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public DoubleQuantity getBigNodes()
  {
    return bigNodes;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetBigNodes(DoubleQuantity newBigNodes, NotificationChain msgs)
  {
    DoubleQuantity oldBigNodes = bigNodes;
    bigNodes = newBigNodes;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__BIG_NODES, oldBigNodes, newBigNodes);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setBigNodes(DoubleQuantity newBigNodes)
  {
    if (newBigNodes != bigNodes)
    {
      NotificationChain msgs = null;
      if (bigNodes != null)
        msgs = ((InternalEObject)bigNodes).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__BIG_NODES, null, msgs);
      if (newBigNodes != null)
        msgs = ((InternalEObject)newBigNodes).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__BIG_NODES, null, msgs);
      msgs = basicSetBigNodes(newBigNodes, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__BIG_NODES, newBigNodes, newBigNodes));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean isBNS()
  {
    return bNS;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setBNS(boolean newBNS)
  {
    boolean oldBNS = bNS;
    bNS = newBNS;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__BNS, oldBNS, bNS));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Size getBigNodeSize()
  {
    return bigNodeSize;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetBigNodeSize(Size newBigNodeSize, NotificationChain msgs)
  {
    Size oldBigNodeSize = bigNodeSize;
    bigNodeSize = newBigNodeSize;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__BIG_NODE_SIZE, oldBigNodeSize, newBigNodeSize);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setBigNodeSize(Size newBigNodeSize)
  {
    if (newBigNodeSize != bigNodeSize)
    {
      NotificationChain msgs = null;
      if (bigNodeSize != null)
        msgs = ((InternalEObject)bigNodeSize).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__BIG_NODE_SIZE, null, msgs);
      if (newBigNodeSize != null)
        msgs = ((InternalEObject)newBigNodeSize).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__BIG_NODE_SIZE, null, msgs);
      msgs = basicSetBigNodeSize(newBigNodeSize, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__BIG_NODE_SIZE, newBigNodeSize, newBigNodeSize));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean isPrio()
  {
    return prio;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setPrio(boolean newPrio)
  {
    boolean oldPrio = prio;
    prio = newPrio;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__PRIO, oldPrio, prio));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Hierarchy getHierarchy()
  {
    return hierarchy;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public NotificationChain basicSetHierarchy(Hierarchy newHierarchy, NotificationChain msgs)
  {
    Hierarchy oldHierarchy = hierarchy;
    hierarchy = newHierarchy;
    if (eNotificationRequired())
    {
      ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__HIERARCHY, oldHierarchy, newHierarchy);
      if (msgs == null) msgs = notification; else msgs.add(notification);
    }
    return msgs;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setHierarchy(Hierarchy newHierarchy)
  {
    if (newHierarchy != hierarchy)
    {
      NotificationChain msgs = null;
      if (hierarchy != null)
        msgs = ((InternalEObject)hierarchy).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__HIERARCHY, null, msgs);
      if (newHierarchy != null)
        msgs = ((InternalEObject)newHierarchy).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - GRandomPackage.CONFIGURATION__HIERARCHY, null, msgs);
      msgs = basicSetHierarchy(newHierarchy, msgs);
      if (msgs != null) msgs.dispatch();
    }
    else if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__HIERARCHY, newHierarchy, newHierarchy));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Integer getSeed()
  {
    return seed;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setSeed(Integer newSeed)
  {
    Integer oldSeed = seed;
    seed = newSeed;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__SEED, oldSeed, seed));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Formats getFormat()
  {
    return format;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setFormat(Formats newFormat)
  {
    Formats oldFormat = format;
    format = newFormat == null ? FORMAT_EDEFAULT : newFormat;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__FORMAT, oldFormat, format));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String getFilename()
  {
    return filename;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setFilename(String newFilename)
  {
    String oldFilename = filename;
    filename = newFilename;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, GRandomPackage.CONFIGURATION__FILENAME, oldFilename, filename));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs)
  {
    switch (featureID)
    {
      case GRandomPackage.CONFIGURATION__NODES:
        return basicSetNodes(null, msgs);
      case GRandomPackage.CONFIGURATION__EDGES:
        return basicSetEdges(null, msgs);
      case GRandomPackage.CONFIGURATION__FRACTION:
        return basicSetFraction(null, msgs);
      case GRandomPackage.CONFIGURATION__BIG_NODES:
        return basicSetBigNodes(null, msgs);
      case GRandomPackage.CONFIGURATION__BIG_NODE_SIZE:
        return basicSetBigNodeSize(null, msgs);
      case GRandomPackage.CONFIGURATION__HIERARCHY:
        return basicSetHierarchy(null, msgs);
    }
    return super.eInverseRemove(otherEnd, featureID, msgs);
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
      case GRandomPackage.CONFIGURATION__SAMPLES:
        return getSamples();
      case GRandomPackage.CONFIGURATION__FORM:
        return getForm();
      case GRandomPackage.CONFIGURATION__NODES:
        return getNodes();
      case GRandomPackage.CONFIGURATION__EDGES:
        return getEdges();
      case GRandomPackage.CONFIGURATION__MW:
        return isMW();
      case GRandomPackage.CONFIGURATION__MAX_WIDTH:
        return getMaxWidth();
      case GRandomPackage.CONFIGURATION__MD:
        return isMD();
      case GRandomPackage.CONFIGURATION__MAX_DEGREE:
        return getMaxDegree();
      case GRandomPackage.CONFIGURATION__PF:
        return isPF();
      case GRandomPackage.CONFIGURATION__FRACTION:
        return getFraction();
      case GRandomPackage.CONFIGURATION__BN:
        return isBN();
      case GRandomPackage.CONFIGURATION__BIG_NODES:
        return getBigNodes();
      case GRandomPackage.CONFIGURATION__BNS:
        return isBNS();
      case GRandomPackage.CONFIGURATION__BIG_NODE_SIZE:
        return getBigNodeSize();
      case GRandomPackage.CONFIGURATION__PRIO:
        return isPrio();
      case GRandomPackage.CONFIGURATION__HIERARCHY:
        return getHierarchy();
      case GRandomPackage.CONFIGURATION__SEED:
        return getSeed();
      case GRandomPackage.CONFIGURATION__FORMAT:
        return getFormat();
      case GRandomPackage.CONFIGURATION__FILENAME:
        return getFilename();
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
      case GRandomPackage.CONFIGURATION__SAMPLES:
        setSamples((Integer)newValue);
        return;
      case GRandomPackage.CONFIGURATION__FORM:
        setForm((Form)newValue);
        return;
      case GRandomPackage.CONFIGURATION__NODES:
        setNodes((Nodes)newValue);
        return;
      case GRandomPackage.CONFIGURATION__EDGES:
        setEdges((Edges)newValue);
        return;
      case GRandomPackage.CONFIGURATION__MW:
        setMW((Boolean)newValue);
        return;
      case GRandomPackage.CONFIGURATION__MAX_WIDTH:
        setMaxWidth((Integer)newValue);
        return;
      case GRandomPackage.CONFIGURATION__MD:
        setMD((Boolean)newValue);
        return;
      case GRandomPackage.CONFIGURATION__MAX_DEGREE:
        setMaxDegree((Integer)newValue);
        return;
      case GRandomPackage.CONFIGURATION__PF:
        setPF((Boolean)newValue);
        return;
      case GRandomPackage.CONFIGURATION__FRACTION:
        setFraction((DoubleQuantity)newValue);
        return;
      case GRandomPackage.CONFIGURATION__BN:
        setBN((Boolean)newValue);
        return;
      case GRandomPackage.CONFIGURATION__BIG_NODES:
        setBigNodes((DoubleQuantity)newValue);
        return;
      case GRandomPackage.CONFIGURATION__BNS:
        setBNS((Boolean)newValue);
        return;
      case GRandomPackage.CONFIGURATION__BIG_NODE_SIZE:
        setBigNodeSize((Size)newValue);
        return;
      case GRandomPackage.CONFIGURATION__PRIO:
        setPrio((Boolean)newValue);
        return;
      case GRandomPackage.CONFIGURATION__HIERARCHY:
        setHierarchy((Hierarchy)newValue);
        return;
      case GRandomPackage.CONFIGURATION__SEED:
        setSeed((Integer)newValue);
        return;
      case GRandomPackage.CONFIGURATION__FORMAT:
        setFormat((Formats)newValue);
        return;
      case GRandomPackage.CONFIGURATION__FILENAME:
        setFilename((String)newValue);
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
      case GRandomPackage.CONFIGURATION__SAMPLES:
        setSamples(SAMPLES_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__FORM:
        setForm(FORM_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__NODES:
        setNodes((Nodes)null);
        return;
      case GRandomPackage.CONFIGURATION__EDGES:
        setEdges((Edges)null);
        return;
      case GRandomPackage.CONFIGURATION__MW:
        setMW(MW_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__MAX_WIDTH:
        setMaxWidth(MAX_WIDTH_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__MD:
        setMD(MD_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__MAX_DEGREE:
        setMaxDegree(MAX_DEGREE_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__PF:
        setPF(PF_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__FRACTION:
        setFraction((DoubleQuantity)null);
        return;
      case GRandomPackage.CONFIGURATION__BN:
        setBN(BN_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__BIG_NODES:
        setBigNodes((DoubleQuantity)null);
        return;
      case GRandomPackage.CONFIGURATION__BNS:
        setBNS(BNS_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__BIG_NODE_SIZE:
        setBigNodeSize((Size)null);
        return;
      case GRandomPackage.CONFIGURATION__PRIO:
        setPrio(PRIO_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__HIERARCHY:
        setHierarchy((Hierarchy)null);
        return;
      case GRandomPackage.CONFIGURATION__SEED:
        setSeed(SEED_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__FORMAT:
        setFormat(FORMAT_EDEFAULT);
        return;
      case GRandomPackage.CONFIGURATION__FILENAME:
        setFilename(FILENAME_EDEFAULT);
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
      case GRandomPackage.CONFIGURATION__SAMPLES:
        return samples != SAMPLES_EDEFAULT;
      case GRandomPackage.CONFIGURATION__FORM:
        return form != FORM_EDEFAULT;
      case GRandomPackage.CONFIGURATION__NODES:
        return nodes != null;
      case GRandomPackage.CONFIGURATION__EDGES:
        return edges != null;
      case GRandomPackage.CONFIGURATION__MW:
        return mW != MW_EDEFAULT;
      case GRandomPackage.CONFIGURATION__MAX_WIDTH:
        return MAX_WIDTH_EDEFAULT == null ? maxWidth != null : !MAX_WIDTH_EDEFAULT.equals(maxWidth);
      case GRandomPackage.CONFIGURATION__MD:
        return mD != MD_EDEFAULT;
      case GRandomPackage.CONFIGURATION__MAX_DEGREE:
        return MAX_DEGREE_EDEFAULT == null ? maxDegree != null : !MAX_DEGREE_EDEFAULT.equals(maxDegree);
      case GRandomPackage.CONFIGURATION__PF:
        return pF != PF_EDEFAULT;
      case GRandomPackage.CONFIGURATION__FRACTION:
        return fraction != null;
      case GRandomPackage.CONFIGURATION__BN:
        return bN != BN_EDEFAULT;
      case GRandomPackage.CONFIGURATION__BIG_NODES:
        return bigNodes != null;
      case GRandomPackage.CONFIGURATION__BNS:
        return bNS != BNS_EDEFAULT;
      case GRandomPackage.CONFIGURATION__BIG_NODE_SIZE:
        return bigNodeSize != null;
      case GRandomPackage.CONFIGURATION__PRIO:
        return prio != PRIO_EDEFAULT;
      case GRandomPackage.CONFIGURATION__HIERARCHY:
        return hierarchy != null;
      case GRandomPackage.CONFIGURATION__SEED:
        return SEED_EDEFAULT == null ? seed != null : !SEED_EDEFAULT.equals(seed);
      case GRandomPackage.CONFIGURATION__FORMAT:
        return format != FORMAT_EDEFAULT;
      case GRandomPackage.CONFIGURATION__FILENAME:
        return FILENAME_EDEFAULT == null ? filename != null : !FILENAME_EDEFAULT.equals(filename);
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

    StringBuilder result = new StringBuilder(super.toString());
    result.append(" (samples: ");
    result.append(samples);
    result.append(", form: ");
    result.append(form);
    result.append(", mW: ");
    result.append(mW);
    result.append(", maxWidth: ");
    result.append(maxWidth);
    result.append(", mD: ");
    result.append(mD);
    result.append(", maxDegree: ");
    result.append(maxDegree);
    result.append(", pF: ");
    result.append(pF);
    result.append(", bN: ");
    result.append(bN);
    result.append(", bNS: ");
    result.append(bNS);
    result.append(", prio: ");
    result.append(prio);
    result.append(", seed: ");
    result.append(seed);
    result.append(", format: ");
    result.append(format);
    result.append(", filename: ");
    result.append(filename);
    result.append(')');
    return result.toString();
  }

} //ConfigurationImpl
