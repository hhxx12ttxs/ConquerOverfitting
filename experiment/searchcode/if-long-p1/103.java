//This did not convert with jd-gui.  Need to copy the raw smali into this file to be converted
 
//SMALI - ResourceAdapter.smali

.class public abstract Lmiui/resourcebrowser/activity/ResourceAdapter;
.super Lmiui/resourcebrowser/widget/AsyncImageAdapter;
.source "ResourceAdapter.java"

# interfaces
.implements Lmiui/resourcebrowser/ResourceConstants;


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lmiui/resourcebrowser/activity/ResourceAdapter$1;,
        Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;
    }
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Lmiui/resourcebrowser/widget/AsyncImageAdapter",
        "<",
        "Lmiui/resourcebrowser/model/Resource;",
        ">;",
        "Lmiui/resourcebrowser/ResourceConstants;"
    }
.end annotation


# static fields
.field private static final PARAMS:Landroid/view/ViewGroup$LayoutParams;


# instance fields
.field private mBatchHandler:Lmiui/resourcebrowser/util/BatchResourceHandler;

.field protected mContext:Landroid/app/Activity;

.field private mDecodeImageLowQuality:Z

.field protected mFragment:Lmiui/resourcebrowser/activity/BaseFragment;

.field private mInflater:Landroid/view/LayoutInflater;

.field private mItemHorizontalSpacing:I

.field private mItemVerticalSpaceing:I

.field private mMusicPlayer:Lmiui/resourcebrowser/util/ResourceMusicPlayer;

.field protected mResContext:Lmiui/resourcebrowser/ResourceContext;

.field protected mResController:Lmiui/resourcebrowser/controller/ResourceController;

.field private mThumbnailDownload:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

.field private mThumbnailHeight:I

.field private mThumbnailWidth:I


# direct methods
.method static constructor <clinit>()V
    .locals 3

    .prologue
    .line 49
    new-instance v0, Landroid/view/ViewGroup$LayoutParams;

    const/4 v1, -0x1

    const/4 v2, -0x2

    invoke-direct {v0, v1, v2}, Landroid/view/ViewGroup$LayoutParams;-><init>(II)V

    sput-object v0, Lmiui/resourcebrowser/activity/ResourceAdapter;->PARAMS:Landroid/view/ViewGroup$LayoutParams;

    return-void
.end method

.method private constructor <init>(Lmiui/resourcebrowser/activity/BaseFragment;Landroid/content/Context;Lmiui/resourcebrowser/ResourceContext;)V
    .locals 2
    .parameter "fragment"
    .parameter "context"
    .parameter "resContext"

    .prologue
    const/4 v0, -0x1

    .line 80
    invoke-direct {p0}, Lmiui/resourcebrowser/widget/AsyncImageAdapter;-><init>()V

    .line 62
    iput v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mItemHorizontalSpacing:I

    .line 63
    iput v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mItemVerticalSpaceing:I

    .line 67
    new-instance v0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    const/4 v1, 0x0

    invoke-direct {v0, p0, v1}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;-><init>(Lmiui/resourcebrowser/activity/ResourceAdapter;Lmiui/resourcebrowser/activity/ResourceAdapter$1;)V

    iput-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mThumbnailDownload:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    .line 81
    iput-object p1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mFragment:Lmiui/resourcebrowser/activity/BaseFragment;

    .line 82
    check-cast p2, Landroid/app/Activity;

    .end local p2
    iput-object p2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    .line 84
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mFragment:Lmiui/resourcebrowser/activity/BaseFragment;

    if-nez v0, :cond_0

    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    if-nez v0, :cond_0

    .line 85
    new-instance v0, Ljava/lang/RuntimeException;

    const-string v1, "invalid parameters: fragment and activity can not both be null."

    invoke-direct {v0, v1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 88
    :cond_0
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    invoke-virtual {v0}, Landroid/app/Activity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const v1, 0x609000f

    invoke-virtual {v0, v1}, Landroid/content/res/Resources;->getBoolean(I)Z

    move-result v0

    iput-boolean v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mDecodeImageLowQuality:Z

    .line 90
    iput-object p3, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    .line 92
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v0}, Lmiui/resourcebrowser/ResourceContext;->getDisplayType()I

    move-result v0

    invoke-static {v0}, Lmiui/resourcebrowser/util/ResourceHelper;->getDataPerLine(I)I

    move-result v0

    invoke-virtual {p0, v0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->setDataPerLine(I)V

    .line 94
    const/4 v0, 0x2

    invoke-virtual {p0, v0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->setAutoLoadMoreStyle(I)V

    .line 95
    const/16 v0, 0x1e

    invoke-virtual {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDataPerLine()I

    move-result v1

    mul-int/lit8 v1, v1, 0x2

    div-int/2addr v0, v1

    invoke-virtual {p0, v0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->setPreloadOffset(I)V

    .line 97
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    invoke-static {v0}, Landroid/view/LayoutInflater;->from(Landroid/content/Context;)Landroid/view/LayoutInflater;

    move-result-object v0

    iput-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mInflater:Landroid/view/LayoutInflater;

    .line 98
    invoke-direct {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->resolveThumbnailSize()V

    .line 99
    return-void
.end method

.method public constructor <init>(Lmiui/resourcebrowser/activity/BaseFragment;Lmiui/resourcebrowser/ResourceContext;)V
    .locals 1
    .parameter "fragment"
    .parameter "resContext"

    .prologue
    .line 72
    invoke-virtual {p1}, Lmiui/resourcebrowser/activity/BaseFragment;->getActivity()Landroid/app/Activity;

    move-result-object v0

    invoke-direct {p0, p1, v0, p2}, Lmiui/resourcebrowser/activity/ResourceAdapter;-><init>(Lmiui/resourcebrowser/activity/BaseFragment;Landroid/content/Context;Lmiui/resourcebrowser/ResourceContext;)V

    .line 73
    return-void
.end method

.method private bindText(Landroid/view/View;ILmiui/resourcebrowser/model/Resource;Ljava/lang/String;)V
    .locals 2
    .parameter "view"
    .parameter "id"
    .parameter "resourceItem"
    .parameter "text"

    .prologue
    .line 301
    invoke-virtual {p1, p2}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    .line 302
    .local v0, textView:Landroid/widget/TextView;
    if-eqz v0, :cond_0

    .line 303
    invoke-virtual {v0, p4}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 304
    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 306
    :cond_0
    return-void
.end method

.method private bindView(Landroid/view/View;Lmiui/resourcebrowser/model/Resource;II)V
    .locals 10
    .parameter "view"
    .parameter "resourceItem"
    .parameter "groupIndex"
    .parameter "group"

    .prologue
    const v9, 0x60b0041

    const/4 v6, 0x4

    const/4 v8, 0x0

    .line 255
    iget-object v5, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v5}, Lmiui/resourcebrowser/ResourceContext;->getDisplayType()I

    move-result v5

    if-ne v5, v6, :cond_5

    .line 256
    invoke-direct {p0, p1, p2, p4}, Lmiui/resourcebrowser/activity/ResourceAdapter;->setResourceFlag(Landroid/view/View;Lmiui/resourcebrowser/model/Resource;I)V

    .line 257
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getTitle()Ljava/lang/String;

    move-result-object v5

    invoke-direct {p0, p1, v9, p2, v5}, Lmiui/resourcebrowser/activity/ResourceAdapter;->bindText(Landroid/view/View;ILmiui/resourcebrowser/model/Resource;Ljava/lang/String;)V

    .line 258
    const v5, 0x60b004a

    invoke-virtual {p1, v5}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v4

    check-cast v4, Landroid/widget/TextView;

    .line 259
    .local v4, tv:Landroid/widget/TextView;
    const-string v5, "duration"

    invoke-virtual {p2, v5}, Lmiui/resourcebrowser/model/Resource;->getExtraMeta(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v1

    .line 260
    .local v1, str:Ljava/lang/String;
    if-eqz v1, :cond_2

    invoke-static {v1}, Ljava/lang/Integer;->parseInt(Ljava/lang/String;)I

    move-result v0

    .line 261
    .local v0, duration:I
    :goto_0
    if-gez v0, :cond_0

    .line 262
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getLocalPath()Ljava/lang/String;

    move-result-object v5

    invoke-static {v5}, Lmiui/resourcebrowser/util/ResourceHelper;->getLocalRingtoneDuration(Ljava/lang/String;)J

    move-result-wide v5

    long-to-int v0, v5

    .line 263
    if-ltz v0, :cond_0

    .line 264
    const-string v5, "duration"

    invoke-static {v0}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;

    move-result-object v6

    invoke-virtual {p2, v5, v6}, Lmiui/resourcebrowser/model/Resource;->putExtraMeta(Ljava/lang/String;Ljava/lang/String;)V

    .line 268
    :cond_0
    if-gez v0, :cond_3

    .line 269
    const/16 v5, 0x8

    invoke-virtual {v4, v5}, Landroid/widget/TextView;->setVisibility(I)V

    .line 298
    .end local v0           #duration:I
    .end local v1           #str:Ljava/lang/String;
    .end local v4           #tv:Landroid/widget/TextView;
    :cond_1
    :goto_1
    return-void

    .line 260
    .restart local v1       #str:Ljava/lang/String;
    .restart local v4       #tv:Landroid/widget/TextView;
    :cond_2
    const/4 v0, -0x1

    goto :goto_0

    .line 270
    .restart local v0       #duration:I
    :cond_3
    if-lez v0, :cond_4

    .line 271
    invoke-virtual {v4, v8}, Landroid/widget/TextView;->setVisibility(I)V

    .line 272
    invoke-static {v0}, Lmiui/resourcebrowser/util/ResourceHelper;->formatDuration(I)Ljava/lang/String;

    move-result-object v5

    invoke-virtual {v4, v5}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    goto :goto_1

    .line 274
    :cond_4
    invoke-virtual {v4, v8}, Landroid/widget/TextView;->setVisibility(I)V

    .line 275
    const v5, 0x60c000b

    invoke-virtual {v4, v5}, Landroid/widget/TextView;->setText(I)V

    goto :goto_1

    .line 280
    .end local v0           #duration:I
    .end local v1           #str:Ljava/lang/String;
    .end local v4           #tv:Landroid/widget/TextView;
    :cond_5
    iget-object v5, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mBatchHandler:Lmiui/resourcebrowser/util/BatchResourceHandler;

    if-eqz v5, :cond_1

    .line 284
    const v5, 0x60b0048

    invoke-virtual {p1, v5}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v2

    check-cast v2, Landroid/widget/ImageView;

    .line 285
    .local v2, thumbnail:Landroid/widget/ImageView;
    if-eqz p2, :cond_6

    .line 286
    new-instance v5, Landroid/util/Pair;

    invoke-static {p3}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v6

    invoke-static {p4}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v7

    invoke-direct {v5, v6, v7}, Landroid/util/Pair;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V

    invoke-virtual {v2, v5}, Landroid/widget/ImageView;->setTag(Ljava/lang/Object;)V

    .line 287
    iget-object v5, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mBatchHandler:Lmiui/resourcebrowser/util/BatchResourceHandler;

    invoke-virtual {v5}, Lmiui/resourcebrowser/util/BatchResourceHandler;->getResourceClickListener()Landroid/view/View$OnClickListener;

    move-result-object v5

    invoke-virtual {v2, v5}, Landroid/widget/ImageView;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 288
    invoke-virtual {v2, v8}, Landroid/widget/ImageView;->setVisibility(I)V

    .line 289
    invoke-direct {p0, p1, p2, p4}, Lmiui/resourcebrowser/activity/ResourceAdapter;->setResourceFlag(Landroid/view/View;Lmiui/resourcebrowser/model/Resource;I)V

    .line 290
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getTitle()Ljava/lang/String;

    move-result-object v5

    invoke-direct {p0, p1, v9, p2, v5}, Lmiui/resourcebrowser/activity/ResourceAdapter;->bindText(Landroid/view/View;ILmiui/resourcebrowser/model/Resource;Ljava/lang/String;)V

    goto :goto_1

    .line 292
    :cond_6
    invoke-virtual {v2}, Landroid/widget/ImageView;->getParent()Landroid/view/ViewParent;

    move-result-object v5

    check-cast v5, Landroid/view/View;

    invoke-virtual {v5, v6}, Landroid/view/View;->setVisibility(I)V

    .line 293
    invoke-virtual {p1, v9}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v3

    .line 294
    .local v3, title:Landroid/view/View;
    if-eqz v3, :cond_1

    .line 295
    invoke-virtual {v3, v6}, Landroid/view/View;->setVisibility(I)V

    goto :goto_1
.end method

.method private resolveThumbnailSize()V
    .locals 6

    .prologue
    .line 131
    iget-object v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    invoke-virtual {v4}, Landroid/app/Activity;->getResources()Landroid/content/res/Resources;

    move-result-object v4

    const v5, 0x602018e

    invoke-virtual {v4, v5}, Landroid/content/res/Resources;->getDrawable(I)Landroid/graphics/drawable/Drawable;

    move-result-object v0

    .line 132
    .local v0, bgItem:Landroid/graphics/drawable/Drawable;
    new-instance v2, Landroid/graphics/Rect;

    invoke-direct {v2}, Landroid/graphics/Rect;-><init>()V

    .line 133
    .local v2, rect:Landroid/graphics/Rect;
    invoke-virtual {v0, v2}, Landroid/graphics/drawable/Drawable;->getPadding(Landroid/graphics/Rect;)Z

    .line 134
    iget v4, v2, Landroid/graphics/Rect;->left:I

    iget v5, v2, Landroid/graphics/Rect;->right:I

    add-int v1, v4, v5

    .line 136
    .local v1, horizontalPadding:I
    iget-object v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    iget-object v5, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v5}, Lmiui/resourcebrowser/ResourceContext;->getDisplayType()I

    move-result v5

    invoke-static {v4, v5, v1}, Lmiui/resourcebrowser/util/ResourceHelper;->getThumbnailSize(Landroid/app/Activity;II)Landroid/util/Pair;

    move-result-object v3

    .line 138
    .local v3, size:Landroid/util/Pair;,"Landroid/util/Pair<Ljava/lang/Integer;Ljava/lang/Integer;>;"
    iget-object v4, v3, Landroid/util/Pair;->first:Ljava/lang/Object;

    check-cast v4, Ljava/lang/Integer;

    invoke-virtual {v4}, Ljava/lang/Integer;->intValue()I

    move-result v4

    iput v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mThumbnailWidth:I

    .line 139
    iget-object v4, v3, Landroid/util/Pair;->second:Ljava/lang/Object;

    check-cast v4, Ljava/lang/Integer;

    invoke-virtual {v4}, Ljava/lang/Integer;->intValue()I

    move-result v4

    iput v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mThumbnailHeight:I

    .line 140
    iget-object v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    iget v5, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mThumbnailWidth:I

    invoke-virtual {v4, v5}, Lmiui/resourcebrowser/ResourceContext;->setThumbnailImageWidth(I)V

    .line 142
    iget-object v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    invoke-static {v4}, Lmiui/resourcebrowser/util/ResourceHelper;->getThumbnailGap(Landroid/content/Context;)I

    move-result v4

    iput v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mItemHorizontalSpacing:I

    .line 143
    iget v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mItemHorizontalSpacing:I

    iput v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mItemVerticalSpaceing:I

    .line 144
    return-void
.end method

.method private setListItemViewBackground(Landroid/view/View;II)V
    .locals 4
    .parameter "view"
    .parameter "groupPos"
    .parameter "group"

    .prologue
    const/4 v3, 0x1

    .line 162
    const/4 v1, 0x0

    .line 163
    .local v1, resId:I
    invoke-virtual {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDataPerLine()I

    move-result v2

    if-ne v2, v3, :cond_3

    .line 164
    invoke-virtual {p0, p3}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getCount(I)I

    move-result v0

    .line 165
    .local v0, dataCnt:I
    if-ne v0, v3, :cond_0

    .line 166
    const v1, 0x602018d

    .line 178
    .end local v0           #dataCnt:I
    :goto_0
    invoke-virtual {p1, v1}, Landroid/view/View;->setBackgroundResource(I)V

    .line 179
    return-void

    .line 167
    .restart local v0       #dataCnt:I
    :cond_0
    add-int/lit8 v2, v0, -0x1

    if-ne p2, v2, :cond_1

    .line 168
    const v1, 0x6020189

    goto :goto_0

    .line 169
    :cond_1
    if-nez p2, :cond_2

    .line 170
    const v1, 0x6020181

    goto :goto_0

    .line 172
    :cond_2
    const v1, 0x6020185

    goto :goto_0

    .line 175
    .end local v0           #dataCnt:I
    :cond_3
    const v1, 0x602018e

    goto :goto_0
.end method

.method private setResourceFlag(Landroid/view/View;Lmiui/resourcebrowser/model/Resource;I)V
    .locals 5
    .parameter "view"
    .parameter "resourceItem"
    .parameter "group"

    .prologue
    .line 309
    if-eqz p2, :cond_2

    .line 310
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getOnlineId()Ljava/lang/String;

    move-result-object v1

    .line 311
    .local v1, onlineId:Ljava/lang/String;
    invoke-static {v1}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v3

    if-nez v3, :cond_0

    .line 312
    iget-object v3, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResController:Lmiui/resourcebrowser/controller/ResourceController;

    invoke-virtual {v3}, Lmiui/resourcebrowser/controller/ResourceController;->getLocalDataManager()Lmiui/resourcebrowser/controller/LocalDataManager;

    move-result-object v3

    invoke-virtual {v3, v1}, Lmiui/resourcebrowser/controller/LocalDataManager;->getResourceByOnlineId(Ljava/lang/String;)Lmiui/resourcebrowser/model/Resource;

    move-result-object v2

    .line 313
    .local v2, r:Lmiui/resourcebrowser/model/Resource;
    if-eqz v2, :cond_0

    .line 314
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getStatus()I

    move-result v3

    or-int/lit8 v3, v3, 0x1

    invoke-virtual {p2, v3}, Lmiui/resourcebrowser/model/Resource;->setStatus(I)V

    .line 317
    .end local v2           #r:Lmiui/resourcebrowser/model/Resource;
    :cond_0
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getLocalId()Ljava/lang/String;

    move-result-object v0

    .line 318
    .local v0, localId:Ljava/lang/String;
    invoke-static {v0}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z

    move-result v3

    if-nez v3, :cond_1

    .line 319
    iget-object v3, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResController:Lmiui/resourcebrowser/controller/ResourceController;

    invoke-virtual {v3}, Lmiui/resourcebrowser/controller/ResourceController;->getOnlineDataManager()Lmiui/resourcebrowser/controller/OnlineDataManager;

    move-result-object v3

    invoke-virtual {v3, v0}, Lmiui/resourcebrowser/controller/OnlineDataManager;->getResourceByLocalId(Ljava/lang/String;)Lmiui/resourcebrowser/model/Resource;

    move-result-object v2

    .line 320
    .restart local v2       #r:Lmiui/resourcebrowser/model/Resource;
    if-eqz v2, :cond_1

    .line 321
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getStatus()I

    move-result v3

    or-int/lit8 v3, v3, 0x2

    invoke-virtual {p2, v3}, Lmiui/resourcebrowser/model/Resource;->setStatus(I)V

    .line 324
    .end local v2           #r:Lmiui/resourcebrowser/model/Resource;
    :cond_1
    const v3, 0x60b004c

    invoke-virtual {p1, v3}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v3

    const/4 v4, 0x0

    invoke-virtual {v3, v4}, Landroid/view/View;->setVisibility(I)V

    .line 325
    const v3, 0x60b004b

    invoke-virtual {p1, v3}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v3

    check-cast v3, Landroid/widget/ImageView;

    invoke-virtual {p0, p2, p3}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getTopFlagId(Lmiui/resourcebrowser/model/Resource;I)I

    move-result v4

    invoke-virtual {v3, v4}, Landroid/widget/ImageView;->setImageResource(I)V

    .line 326
    const v3, 0x60b0063

    invoke-virtual {p1, v3}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v3

    check-cast v3, Landroid/widget/ImageView;

    invoke-virtual {p0, p2, p3}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getCenterFlagId(Lmiui/resourcebrowser/model/Resource;I)I

    move-result v4

    invoke-virtual {v3, v4}, Landroid/widget/ImageView;->setImageResource(I)V

    .line 327
    const v3, 0x60b0066

    invoke-virtual {p1, v3}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v3

    check-cast v3, Landroid/widget/ImageView;

    invoke-virtual {p0, p2, p3}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getBottomFlagId(Lmiui/resourcebrowser/model/Resource;I)I

    move-result v4

    invoke-virtual {v3, v4}, Landroid/widget/ImageView;->setImageResource(I)V

    .line 329
    .end local v0           #localId:Ljava/lang/String;
    .end local v1           #onlineId:Ljava/lang/String;
    :cond_2
    return-void
.end method

.method private setThumbnail(Landroid/widget/ImageView;Lmiui/resourcebrowser/model/Resource;ILjava/util/List;Z)V
    .locals 3
    .parameter "view"
    .parameter "data"
    .parameter "index"
    .parameter
    .parameter "showEmpty"
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroid/widget/ImageView;",
            "Lmiui/resourcebrowser/model/Resource;",
            "I",
            "Ljava/util/List",
            "<",
            "Ljava/lang/Object;",
            ">;Z)V"
        }
    .end annotation

    .prologue
    .line 434
    .local p4, partialData:Ljava/util/List;,"Ljava/util/List<Ljava/lang/Object;>;"
    if-nez p1, :cond_0

    .line 450
    :goto_0
    return-void

    .line 437
    :cond_0
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v0}, Lmiui/resourcebrowser/ResourceContext;->getResourceFormat()I

    move-result v0

    const/4 v1, 0x3

    if-ne v0, v1, :cond_2

    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mBatchHandler:Lmiui/resourcebrowser/util/BatchResourceHandler;

    if-nez v0, :cond_2

    .line 438
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mMusicPlayer:Lmiui/resourcebrowser/util/ResourceMusicPlayer;

    if-nez v0, :cond_1

    .line 439
    new-instance v0, Lmiui/resourcebrowser/util/ResourceMusicPlayer;

    iget-object v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    const/4 v2, 0x1

    invoke-direct {v0, v1, v2}, Lmiui/resourcebrowser/util/ResourceMusicPlayer;-><init>(Landroid/app/Activity;Z)V

    iput-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mMusicPlayer:Lmiui/resourcebrowser/util/ResourceMusicPlayer;

    .line 441
    :cond_1
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mMusicPlayer:Lmiui/resourcebrowser/util/ResourceMusicPlayer;

    invoke-virtual {v0, p1, p2}, Lmiui/resourcebrowser/util/ResourceMusicPlayer;->initPlayButtonIfNeed(Landroid/widget/ImageView;Lmiui/resourcebrowser/model/Resource;)V

    goto :goto_0

    .line 445
    :cond_2
    if-eqz p4, :cond_3

    invoke-interface {p4}, Ljava/util/List;->isEmpty()Z

    move-result v0

    if-eqz v0, :cond_4

    .line 446
    :cond_3
    const/4 v0, 0x0

    invoke-virtual {p1, v0}, Landroid/widget/ImageView;->setImageBitmap(Landroid/graphics/Bitmap;)V

    goto :goto_0

    .line 448
    :cond_4
    invoke-interface {p4, p3}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/graphics/Bitmap;

    invoke-virtual {p1, v0}, Landroid/widget/ImageView;->setImageBitmap(Landroid/graphics/Bitmap;)V

    goto :goto_0
.end method


# virtual methods
.method protected bindContentView(Landroid/view/View;Ljava/util/List;III)Landroid/view/View;
    .locals 15
    .parameter "view"
    .parameter
    .parameter "position"
    .parameter "groupPos"
    .parameter "group"
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroid/view/View;",
            "Ljava/util/List",
            "<",
            "Lmiui/resourcebrowser/model/Resource;",
            ">;III)",
            "Landroid/view/View;"
        }
    .end annotation

    .prologue
    .line 188
    .local p2, data:Ljava/util/List;,"Ljava/util/List<Lmiui/resourcebrowser/model/Resource;>;"
    move-object/from16 v8, p1

    check-cast v8, Landroid/widget/LinearLayout;

    .line 189
    .local v8, root:Landroid/widget/LinearLayout;
    if-nez v8, :cond_1

    .line 190
    new-instance v8, Landroid/widget/LinearLayout;

    .end local v8           #root:Landroid/widget/LinearLayout;
    iget-object v11, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    invoke-direct {v8, v11}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    .line 191
    .restart local v8       #root:Landroid/widget/LinearLayout;
    const/4 v11, 0x1

    invoke-virtual {v8, v11}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 192
    new-instance v10, Landroid/widget/TextView;

    iget-object v11, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    const/4 v12, 0x0

    const v13, 0x1010208

    invoke-direct {v10, v11, v12, v13}, Landroid/widget/TextView;-><init>(Landroid/content/Context;Landroid/util/AttributeSet;I)V

    .line 193
    .local v10, tv:Landroid/widget/TextView;
    const v11, 0x6020197

    invoke-virtual {v10, v11}, Landroid/widget/TextView;->setBackgroundResource(I)V

    .line 194
    const/16 v11, 0x8

    invoke-virtual {v10, v11}, Landroid/widget/TextView;->setVisibility(I)V

    .line 195
    sget-object v11, Lmiui/resourcebrowser/activity/ResourceAdapter;->PARAMS:Landroid/view/ViewGroup$LayoutParams;

    invoke-virtual {v8, v10, v11}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 197
    new-instance v2, Landroid/widget/LinearLayout;

    iget-object v11, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    invoke-direct {v2, v11}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    .line 198
    .local v2, dataLayout:Landroid/widget/LinearLayout;
    const/4 v11, 0x0

    invoke-virtual {v2, v11}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 199
    const/16 v11, 0x11

    invoke-virtual {v2, v11}, Landroid/widget/LinearLayout;->setGravity(I)V

    .line 200
    sget-object v11, Lmiui/resourcebrowser/activity/ResourceAdapter;->PARAMS:Landroid/view/ViewGroup$LayoutParams;

    invoke-virtual {v8, v2, v11}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 202
    invoke-virtual {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDataPerLine()I

    move-result v4

    .line 203
    .local v4, horizontalTotalItem:I
    const/4 v5, 0x0

    .local v5, i:I
    :goto_0
    if-ge v5, v4, :cond_0

    .line 204
    iget-object v11, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mInflater:Landroid/view/LayoutInflater;

    iget-object v12, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v12}, Lmiui/resourcebrowser/ResourceContext;->getDisplayType()I

    move-result v12

    invoke-static {v12}, Lmiui/resourcebrowser/util/ResourceHelper;->getThumbnailViewResource(I)I

    move-result v12

    const/4 v13, 0x0

    invoke-virtual {v11, v12, v13}, Landroid/view/LayoutInflater;->inflate(ILandroid/view/ViewGroup;)Landroid/view/View;

    move-result-object v6

    .line 206
    .local v6, itemView:Landroid/view/View;
    invoke-virtual {p0, v6, v5, v4}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getItemViewLayoutParams(Landroid/view/View;II)Landroid/widget/LinearLayout$LayoutParams;

    move-result-object v11

    invoke-virtual {v2, v6, v11}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;Landroid/view/ViewGroup$LayoutParams;)V

    .line 203
    add-int/lit8 v5, v5, 0x1

    goto :goto_0

    .line 208
    .end local v6           #itemView:Landroid/view/View;
    :cond_0
    invoke-virtual {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDataPerLine()I

    move-result v11

    const/4 v12, 0x1

    if-gt v11, v12, :cond_3

    const/4 v11, 0x1

    :goto_1
    invoke-virtual {v2, v11}, Landroid/widget/LinearLayout;->setEnabled(Z)V

    .line 209
    const/4 v11, 0x1

    invoke-virtual {v8, v11}, Landroid/widget/LinearLayout;->setDrawingCacheEnabled(Z)V

    .line 212
    .end local v2           #dataLayout:Landroid/widget/LinearLayout;
    .end local v4           #horizontalTotalItem:I
    .end local v5           #i:I
    .end local v10           #tv:Landroid/widget/TextView;
    :cond_1
    const/4 v11, 0x0

    invoke-virtual {v8, v11}, Landroid/widget/LinearLayout;->getChildAt(I)Landroid/view/View;

    move-result-object v3

    check-cast v3, Landroid/widget/TextView;

    .line 213
    .local v3, dividerText:Landroid/widget/TextView;
    const/16 v11, 0x8

    invoke-virtual {v3, v11}, Landroid/widget/TextView;->setVisibility(I)V

    .line 215
    const/4 v11, 0x1

    invoke-virtual {v8, v11}, Landroid/widget/LinearLayout;->getChildAt(I)Landroid/view/View;

    move-result-object v2

    check-cast v2, Landroid/widget/LinearLayout;

    .line 216
    .restart local v2       #dataLayout:Landroid/widget/LinearLayout;
    move/from16 v0, p4

    move/from16 v1, p5

    invoke-direct {p0, v2, v0, v1}, Lmiui/resourcebrowser/activity/ResourceAdapter;->setListItemViewBackground(Landroid/view/View;II)V

    .line 217
    invoke-virtual {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDataPerLine()I

    move-result v11

    const/4 v12, 0x1

    if-ne v11, v12, :cond_2

    .line 218
    new-instance v11, Landroid/util/Pair;

    invoke-virtual {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDataPerLine()I

    move-result v12

    mul-int v12, v12, p4

    invoke-static {v12}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v12

    invoke-static/range {p5 .. p5}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v13

    invoke-direct {v11, v12, v13}, Landroid/util/Pair;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V

    invoke-virtual {v2, v11}, Landroid/widget/LinearLayout;->setTag(Ljava/lang/Object;)V

    .line 219
    iget-object v11, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mBatchHandler:Lmiui/resourcebrowser/util/BatchResourceHandler;

    if-eqz v11, :cond_2

    .line 220
    iget-object v11, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mBatchHandler:Lmiui/resourcebrowser/util/BatchResourceHandler;

    invoke-virtual {v11}, Lmiui/resourcebrowser/util/BatchResourceHandler;->getResourceClickListener()Landroid/view/View$OnClickListener;

    move-result-object v11

    invoke-virtual {v2, v11}, Landroid/widget/LinearLayout;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    .line 224
    :cond_2
    const/4 v5, 0x0

    .restart local v5       #i:I
    :goto_2
    invoke-virtual {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDataPerLine()I

    move-result v11

    if-ge v5, v11, :cond_5

    .line 225
    invoke-virtual {v2, v5}, Landroid/widget/LinearLayout;->getChildAt(I)Landroid/view/View;

    move-result-object v6

    .line 226
    .restart local v6       #itemView:Landroid/view/View;
    invoke-interface/range {p2 .. p2}, Ljava/util/List;->size()I

    move-result v11

    if-ge v5, v11, :cond_4

    .line 227
    const/4 v11, 0x0

    invoke-virtual {v6, v11}, Landroid/view/View;->setVisibility(I)V

    .line 228
    move-object/from16 v0, p2

    invoke-interface {v0, v5}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v7

    check-cast v7, Lmiui/resourcebrowser/model/Resource;

    .line 229
    .local v7, res:Lmiui/resourcebrowser/model/Resource;
    invoke-virtual {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDataPerLine()I

    move-result v11

    mul-int v11, v11, p4

    add-int/2addr v11, v5

    move/from16 v0, p5

    invoke-direct {p0, v6, v7, v11, v0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->bindView(Landroid/view/View;Lmiui/resourcebrowser/model/Resource;II)V

    .line 224
    .end local v7           #res:Lmiui/resourcebrowser/model/Resource;
    :goto_3
    add-int/lit8 v5, v5, 0x1

    goto :goto_2

    .line 208
    .end local v3           #dividerText:Landroid/widget/TextView;
    .end local v6           #itemView:Landroid/view/View;
    .restart local v4       #horizontalTotalItem:I
    .restart local v10       #tv:Landroid/widget/TextView;
    :cond_3
    const/4 v11, 0x0

    goto :goto_1

    .line 231
    .end local v4           #horizontalTotalItem:I
    .end local v10           #tv:Landroid/widget/TextView;
    .restart local v3       #dividerText:Landroid/widget/TextView;
    .restart local v6       #itemView:Landroid/view/View;
    :cond_4
    const/4 v11, 0x4

    invoke-virtual {v6, v11}, Landroid/view/View;->setVisibility(I)V

    goto :goto_3

    .line 235
    .end local v6           #itemView:Landroid/view/View;
    :cond_5
    if-nez p4, :cond_6

    .line 236
    move/from16 v0, p5

    invoke-virtual {p0, v0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getGroupTitle(I)Ljava/lang/String;

    move-result-object v9

    .line 237
    .local v9, text:Ljava/lang/String;
    if-eqz v9, :cond_6

    .line 238
    invoke-virtual {v3, v9}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 239
    const/4 v11, 0x0

    invoke-virtual {v3, v11}, Landroid/widget/TextView;->setVisibility(I)V

    .line 243
    .end local v9           #text:Ljava/lang/String;
    :cond_6
    iget-object v11, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v11}, Lmiui/resourcebrowser/ResourceContext;->getDisplayType()I

    move-result v11

    const/4 v12, 0x4

    if-ne v11, v12, :cond_7

    .line 244
    iget-object v11, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mBatchHandler:Lmiui/resourcebrowser/util/BatchResourceHandler;

    new-instance v12, Landroid/util/Pair;

    invoke-virtual {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDataPerLine()I

    move-result v13

    mul-int v13, v13, p4

    invoke-static {v13}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v13

    invoke-static/range {p5 .. p5}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v14

    invoke-direct {v12, v13, v14}, Landroid/util/Pair;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V

    invoke-virtual {v11, v2, v12}, Lmiui/resourcebrowser/util/BatchResourceHandler;->initViewState(Landroid/view/View;Landroid/util/Pair;)V

    .line 247
    :cond_7
    return-object v8
.end method

.method protected bridge synthetic bindPartialContentView(Landroid/view/View;Ljava/lang/Object;ILjava/util/List;)V
    .locals 0
    .parameter "x0"
    .parameter "x1"
    .parameter "x2"
    .parameter "x3"

    .prologue
    .line 47
    check-cast p2, Lmiui/resourcebrowser/model/Resource;

    .end local p2
    invoke-virtual {p0, p1, p2, p3, p4}, Lmiui/resourcebrowser/activity/ResourceAdapter;->bindPartialContentView(Landroid/view/View;Lmiui/resourcebrowser/model/Resource;ILjava/util/List;)V

    return-void
.end method

.method protected bindPartialContentView(Landroid/view/View;Lmiui/resourcebrowser/model/Resource;ILjava/util/List;)V
    .locals 7
    .parameter "view"
    .parameter "data"
    .parameter "offset"
    .parameter
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroid/view/View;",
            "Lmiui/resourcebrowser/model/Resource;",
            "I",
            "Ljava/util/List",
            "<",
            "Ljava/lang/Object;",
            ">;)V"
        }
    .end annotation

    .prologue
    .line 421
    .local p4, partialData:Ljava/util/List;,"Ljava/util/List<Ljava/lang/Object;>;"
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v0}, Lmiui/resourcebrowser/ResourceContext;->getDisplayType()I

    move-result v0

    invoke-static {v0}, Lmiui/resourcebrowser/util/ResourceHelper;->isMultipleView(I)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 422
    const v0, 0x60b0049

    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/ImageView;

    const/4 v3, 0x0

    const/4 v5, 0x1

    move-object v0, p0

    move-object v2, p2

    move-object v4, p4

    invoke-direct/range {v0 .. v5}, Lmiui/resourcebrowser/activity/ResourceAdapter;->setThumbnail(Landroid/widget/ImageView;Lmiui/resourcebrowser/model/Resource;ILjava/util/List;Z)V

    .line 423
    const v0, 0x60b0067

    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/ImageView;

    const/4 v3, 0x1

    const/4 v5, 0x0

    move-object v0, p0

    move-object v2, p2

    move-object v4, p4

    invoke-direct/range {v0 .. v5}, Lmiui/resourcebrowser/activity/ResourceAdapter;->setThumbnail(Landroid/widget/ImageView;Lmiui/resourcebrowser/model/Resource;ILjava/util/List;Z)V

    .line 424
    const v0, 0x60b0068

    invoke-virtual {p1, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/ImageView;

    const/4 v3, 0x2

    const/4 v5, 0x0

    move-object v0, p0

    move-object v2, p2

    move-object v4, p4

    invoke-direct/range {v0 .. v5}, Lmiui/resourcebrowser/activity/ResourceAdapter;->setThumbnail(Landroid/widget/ImageView;Lmiui/resourcebrowser/model/Resource;ILjava/util/List;Z)V

    .line 430
    .end local p1
    :goto_0
    return-void

    .line 426
    .restart local p1
    :cond_0
    check-cast p1, Landroid/widget/LinearLayout;

    .end local p1
    const/4 v0, 0x1

    invoke-virtual {p1, v0}, Landroid/widget/LinearLayout;->getChildAt(I)Landroid/view/View;

    move-result-object v6

    .line 427
    .local v6, itemView:Landroid/view/View;
    check-cast v6, Landroid/widget/LinearLayout;

    .end local v6           #itemView:Landroid/view/View;
    invoke-virtual {v6, p3}, Landroid/widget/LinearLayout;->getChildAt(I)Landroid/view/View;

    move-result-object v6

    .line 428
    .restart local v6       #itemView:Landroid/view/View;
    const v0, 0x60b0048

    invoke-virtual {v6, v0}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/ImageView;

    const/4 v3, 0x0

    const/4 v5, 0x1

    move-object v0, p0

    move-object v2, p2

    move-object v4, p4

    invoke-direct/range {v0 .. v5}, Lmiui/resourcebrowser/activity/ResourceAdapter;->setThumbnail(Landroid/widget/ImageView;Lmiui/resourcebrowser/model/Resource;ILjava/util/List;Z)V

    goto :goto_0
.end method

.method public clean()V
    .locals 1

    .prologue
    .line 475
    invoke-super {p0}, Lmiui/resourcebrowser/widget/AsyncImageAdapter;->clean()V

    .line 476
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mMusicPlayer:Lmiui/resourcebrowser/util/ResourceMusicPlayer;

    if-eqz v0, :cond_0

    .line 477
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mMusicPlayer:Lmiui/resourcebrowser/util/ResourceMusicPlayer;

    invoke-virtual {v0}, Lmiui/resourcebrowser/util/ResourceMusicPlayer;->stopMusic()V

    .line 479
    :cond_0
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mThumbnailDownload:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    invoke-virtual {v0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->clean()V

    .line 480
    return-void
.end method

.method public clearDataSet()V
    .locals 1

    .prologue
    .line 112
    invoke-super {p0}, Lmiui/resourcebrowser/widget/AsyncImageAdapter;->clearDataSet()V

    .line 113
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mBatchHandler:Lmiui/resourcebrowser/util/BatchResourceHandler;

    if-eqz v0, :cond_0

    .line 114
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mBatchHandler:Lmiui/resourcebrowser/util/BatchResourceHandler;

    invoke-virtual {v0}, Lmiui/resourcebrowser/util/BatchResourceHandler;->quitEditMode()V

    .line 116
    :cond_0
    return-void
.end method

.method protected downloadThumbnail(Lmiui/resourcebrowser/model/PathEntry;)V
    .locals 2
    .parameter "path"

    .prologue
    .line 463
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v0}, Lmiui/resourcebrowser/ResourceContext;->getResourceFormat()I

    move-result v0

    const/4 v1, 0x3

    if-ne v0, v1, :cond_0

    .line 467
    :goto_0
    return-void

    .line 466
    :cond_0
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mThumbnailDownload:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    invoke-virtual {v0, p1}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->submitDownloadJob(Lmiui/resourcebrowser/model/PathEntry;)V

    goto :goto_0
.end method

.method protected getBottomFlagId(Lmiui/resourcebrowser/model/Resource;I)I
    .locals 1
    .parameter "resourceItem"
    .parameter "group"

    .prologue
    .line 346
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v0}, Lmiui/resourcebrowser/ResourceContext;->getDisplayType()I

    move-result v0

    invoke-static {v0}, Lmiui/resourcebrowser/util/ResourceHelper;->isCombineView(I)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 347
    invoke-virtual {p0, p1, p2}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDownloadableFlag(Lmiui/resourcebrowser/model/Resource;I)I

    move-result v0

    .line 349
    :goto_0
    return v0

    :cond_0
    const/4 v0, 0x0

    goto :goto_0
.end method

.method protected bridge synthetic getCacheKeys(Ljava/lang/Object;)Ljava/util/List;
    .locals 1
    .parameter "x0"

    .prologue
    .line 47
    check-cast p1, Lmiui/resourcebrowser/model/Resource;

    .end local p1
    invoke-virtual {p0, p1}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getCacheKeys(Lmiui/resourcebrowser/model/Resource;)Ljava/util/List;

    move-result-object v0

    return-object v0
.end method

.method protected getCacheKeys(Lmiui/resourcebrowser/model/Resource;)Ljava/util/List;
    .locals 7
    .parameter "data"
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Lmiui/resourcebrowser/model/Resource;",
            ")",
            "Ljava/util/List",
            "<",
            "Ljava/lang/Object;",
            ">;"
        }
    .end annotation

    .prologue
    .line 373
    new-instance v1, Ljava/util/ArrayList;

    invoke-direct {v1}, Ljava/util/ArrayList;-><init>()V

    .line 374
    .local v1, cacheKeys:Ljava/util/List;,"Ljava/util/List<Ljava/lang/Object;>;"
    iget-object v6, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v6}, Lmiui/resourcebrowser/ResourceContext;->getDisplayType()I

    move-result v6

    invoke-static {v6}, Lmiui/resourcebrowser/util/ResourceHelper;->isMultipleView(I)Z

    move-result v6

    if-eqz v6, :cond_1

    const/4 v5, 0x3

    .line 375
    .local v5, total:I
    :goto_0
    invoke-virtual {p1}, Lmiui/resourcebrowser/model/Resource;->getThumbnails()Ljava/util/List;

    move-result-object v4

    .line 376
    .local v4, thumbnails:Ljava/util/List;,"Ljava/util/List<Lmiui/resourcebrowser/model/PathEntry;>;"
    invoke-interface {v4}, Ljava/util/List;->size()I

    move-result v6

    if-lez v6, :cond_2

    .line 377
    invoke-interface {v4}, Ljava/util/List;->size()I

    move-result v6

    invoke-static {v5, v6}, Ljava/lang/Math;->min(II)I

    move-result v5

    .line 378
    const/4 v2, 0x0

    .local v2, i:I
    :goto_1
    if-ge v2, v5, :cond_4

    .line 379
    invoke-interface {v4, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v6

    check-cast v6, Lmiui/resourcebrowser/model/PathEntry;

    invoke-virtual {v6}, Lmiui/resourcebrowser/model/PathEntry;->getLocalPath()Ljava/lang/String;

    move-result-object v3

    .line 380
    .local v3, thumbnailPath:Ljava/lang/String;
    if-eqz v3, :cond_0

    .line 381
    invoke-interface {v1, v3}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 378
    :cond_0
    add-int/lit8 v2, v2, 0x1

    goto :goto_1

    .line 374
    .end local v2           #i:I
    .end local v3           #thumbnailPath:Ljava/lang/String;
    .end local v4           #thumbnails:Ljava/util/List;,"Ljava/util/List<Lmiui/resourcebrowser/model/PathEntry;>;"
    .end local v5           #total:I
    :cond_1
    const/4 v5, 0x1

    goto :goto_0

    .line 385
    .restart local v4       #thumbnails:Ljava/util/List;,"Ljava/util/List<Lmiui/resourcebrowser/model/PathEntry;>;"
    .restart local v5       #total:I
    :cond_2
    invoke-virtual {p1}, Lmiui/resourcebrowser/model/Resource;->getBuildInThumbnails()Ljava/util/List;

    move-result-object v0

    .line 386
    .local v0, buildInThumbnails:Ljava/util/List;,"Ljava/util/List<Ljava/lang/String;>;"
    invoke-interface {v0}, Ljava/util/List;->size()I

    move-result v6

    invoke-static {v5, v6}, Ljava/lang/Math;->min(II)I

    move-result v5

    .line 387
    const/4 v2, 0x0

    .restart local v2       #i:I
    :goto_2
    if-ge v2, v5, :cond_4

    .line 388
    invoke-interface {v0, v2}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Ljava/lang/String;

    .line 389
    .restart local v3       #thumbnailPath:Ljava/lang/String;
    if-eqz v3, :cond_3

    .line 390
    invoke-interface {v1, v3}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    .line 387
    :cond_3
    add-int/lit8 v2, v2, 0x1

    goto :goto_2

    .line 394
    .end local v0           #buildInThumbnails:Ljava/util/List;,"Ljava/util/List<Ljava/lang/String;>;"
    .end local v3           #thumbnailPath:Ljava/lang/String;
    :cond_4
    return-object v1
.end method

.method protected getCenterFlagId(Lmiui/resourcebrowser/model/Resource;I)I
    .locals 1
    .parameter "resourceItem"
    .parameter "group"

    .prologue
    .line 339
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v0}, Lmiui/resourcebrowser/ResourceContext;->getDisplayType()I

    move-result v0

    invoke-static {v0}, Lmiui/resourcebrowser/util/ResourceHelper;->isSingleView(I)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 340
    invoke-virtual {p0, p1, p2}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getDownloadableFlag(Lmiui/resourcebrowser/model/Resource;I)I

    move-result v0

    .line 342
    :goto_0
    return v0

    :cond_0
    const/4 v0, 0x0

    goto :goto_0
.end method

.method protected getDownloadableFlag(Lmiui/resourcebrowser/model/Resource;I)I
    .locals 2
    .parameter "resourceItem"
    .parameter "group"

    .prologue
    .line 360
    if-eqz p1, :cond_1

    invoke-virtual {p1}, Lmiui/resourcebrowser/model/Resource;->getStatus()I

    move-result v1

    invoke-static {v1}, Lmiui/resourcebrowser/util/ResourceHelper;->isLocalResource(I)Z

    move-result v1

    if-eqz v1, :cond_1

    .line 361
    invoke-virtual {p1}, Lmiui/resourcebrowser/model/Resource;->getLocalPath()Ljava/lang/String;

    move-result-object v0

    .line 362
    .local v0, localPath:Ljava/lang/String;
    if-eqz v0, :cond_0

    iget-object v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v1}, Lmiui/resourcebrowser/ResourceContext;->getCurrentUsingPath()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 363
    const v1, 0x602003c

    .line 368
    .end local v0           #localPath:Ljava/lang/String;
    :goto_0
    return v1

    .line 365
    .restart local v0       #localPath:Ljava/lang/String;
    :cond_0
    const v1, 0x602003a

    goto :goto_0

    .line 368
    .end local v0           #localPath:Ljava/lang/String;
    :cond_1
    const/4 v1, 0x0

    goto :goto_0
.end method

.method protected getGroupTitle(I)Ljava/lang/String;
    .locals 1
    .parameter "group"

    .prologue
    .line 251
    const/4 v0, 0x0

    return-object v0
.end method

.method protected getItemViewLayoutParams(Landroid/view/View;II)Landroid/widget/LinearLayout$LayoutParams;
    .locals 4
    .parameter "itemView"
    .parameter "horizontalPos"
    .parameter "horizontalCount"

    .prologue
    const/4 v2, 0x0

    .line 151
    new-instance v0, Landroid/widget/LinearLayout$LayoutParams;

    iget v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mThumbnailWidth:I

    iget v3, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mThumbnailHeight:I

    invoke-direct {v0, v1, v3}, Landroid/widget/LinearLayout$LayoutParams;-><init>(II)V

    .line 152
    .local v0, param:Landroid/widget/LinearLayout$LayoutParams;
    const/4 v1, 0x2

    if-lt p3, v1, :cond_0

    .line 153
    iput v2, v0, Landroid/widget/LinearLayout$LayoutParams;->leftMargin:I

    .line 154
    add-int/lit8 v1, p3, -0x1

    if-ge p2, v1, :cond_1

    iget v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mItemHorizontalSpacing:I

    :goto_0
    iput v1, v0, Landroid/widget/LinearLayout$LayoutParams;->rightMargin:I

    .line 155
    iget v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mItemVerticalSpaceing:I

    iput v1, v0, Landroid/widget/LinearLayout$LayoutParams;->topMargin:I

    .line 156
    iput v2, v0, Landroid/widget/LinearLayout$LayoutParams;->bottomMargin:I

    .line 158
    :cond_0
    return-object v0

    :cond_1
    move v1, v2

    .line 154
    goto :goto_0
.end method

.method protected getLoadPartialDataTask()Lmiui/resourcebrowser/widget/AsyncAdapter$AsyncLoadPartialDataTask;
    .locals 4
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()",
            "Lmiui/resourcebrowser/widget/AsyncAdapter",
            "<",
            "Lmiui/resourcebrowser/model/Resource;",
            ">.Async",
            "LoadPartialDataTask;"
        }
    .end annotation

    .prologue
    const/4 v3, 0x1

    .line 454
    new-instance v0, Lmiui/resourcebrowser/widget/AsyncImageAdapter$AsyncLoadImageTask;

    invoke-direct {v0, p0}, Lmiui/resourcebrowser/widget/AsyncImageAdapter$AsyncLoadImageTask;-><init>(Lmiui/resourcebrowser/widget/AsyncImageAdapter;)V

    .line 455
    .local v0, task:Lmiui/resourcebrowser/widget/AsyncImageAdapter$AsyncLoadImageTask;,"Lmiui/resourcebrowser/widget/AsyncImageAdapter<Lmiui/resourcebrowser/model/Resource;>.AsyncLoadImageTask;"
    invoke-virtual {v0, v3}, Lmiui/resourcebrowser/widget/AsyncImageAdapter$AsyncLoadImageTask;->setAutoStop(Z)V

    .line 456
    new-instance v1, Lmiui/os/DaemonAsyncTask$StackJobPool;

    invoke-direct {v1}, Lmiui/os/DaemonAsyncTask$StackJobPool;-><init>()V

    invoke-virtual {v0, v1}, Lmiui/resourcebrowser/widget/AsyncImageAdapter$AsyncLoadImageTask;->setJobPool(Lmiui/os/DaemonAsyncTask$JobPool;)V

    .line 457
    iget v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mThumbnailWidth:I

    iget v2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mThumbnailHeight:I

    invoke-virtual {v0, v1, v2}, Lmiui/resourcebrowser/widget/AsyncImageAdapter$AsyncLoadImageTask;->setTargetSize(II)V

    .line 458
    invoke-virtual {v0, v3}, Lmiui/resourcebrowser/widget/AsyncImageAdapter$AsyncLoadImageTask;->setScaled(Z)V

    .line 459
    return-object v0
.end method

.method protected final getRegisterAsyncTaskObserver()Lmiui/os/AsyncTaskObserver;
    .locals 1
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "()",
            "Lmiui/os/AsyncTaskObserver",
            "<",
            "Ljava/lang/Void;",
            "Lmiui/resourcebrowser/model/Resource;",
            "Ljava/util/List",
            "<",
            "Lmiui/resourcebrowser/model/Resource;",
            ">;>;"
        }
    .end annotation

    .prologue
    .line 107
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mFragment:Lmiui/resourcebrowser/activity/BaseFragment;

    if-eqz v0, :cond_0

    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mFragment:Lmiui/resourcebrowser/activity/BaseFragment;

    :goto_0
    check-cast v0, Lmiui/os/AsyncTaskObserver;

    return-object v0

    :cond_0
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mContext:Landroid/app/Activity;

    goto :goto_0
.end method

.method protected getTopFlagId(Lmiui/resourcebrowser/model/Resource;I)I
    .locals 1
    .parameter "resourceItem"
    .parameter "group"

    .prologue
    .line 332
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v0}, Lmiui/resourcebrowser/ResourceContext;->getDisplayType()I

    move-result v0

    invoke-static {v0}, Lmiui/resourcebrowser/util/ResourceHelper;->isCombineView(I)Z

    move-result v0

    if-eqz v0, :cond_0

    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResContext:Lmiui/resourcebrowser/ResourceContext;

    invoke-virtual {v0}, Lmiui/resourcebrowser/ResourceContext;->isVersionSupported()Z

    move-result v0

    if-eqz v0, :cond_0

    .line 333
    invoke-virtual {p0, p1, p2}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getUpdatableFlag(Lmiui/resourcebrowser/model/Resource;I)I

    move-result v0

    .line 335
    :goto_0
    return v0

    :cond_0
    const/4 v0, 0x0

    goto :goto_0
.end method

.method protected getUpdatableFlag(Lmiui/resourcebrowser/model/Resource;I)I
    .locals 1
    .parameter "resourceItem"
    .parameter "group"

    .prologue
    .line 353
    if-eqz p1, :cond_0

    invoke-virtual {p1}, Lmiui/resourcebrowser/model/Resource;->getStatus()I

    move-result v0

    invoke-static {v0}, Lmiui/resourcebrowser/util/ResourceHelper;->isOldResource(I)Z

    move-result v0

    if-eqz v0, :cond_0

    .line 354
    const v0, 0x602003b

    .line 356
    :goto_0
    return v0

    :cond_0
    const/4 v0, 0x0

    goto :goto_0
.end method

.method public isEnabled(I)Z
    .locals 1
    .parameter "position"

    .prologue
    .line 183
    const/4 v0, 0x0

    return v0
.end method

.method protected bridge synthetic isValidKey(Ljava/lang/Object;Ljava/lang/Object;I)Z
    .locals 1
    .parameter "x0"
    .parameter "x1"
    .parameter "x2"

    .prologue
    .line 47
    check-cast p2, Lmiui/resourcebrowser/model/Resource;

    .end local p2
    invoke-virtual {p0, p1, p2, p3}, Lmiui/resourcebrowser/activity/ResourceAdapter;->isValidKey(Ljava/lang/Object;Lmiui/resourcebrowser/model/Resource;I)Z

    move-result v0

    return v0
.end method

.method protected isValidKey(Ljava/lang/Object;Lmiui/resourcebrowser/model/Resource;I)Z
    .locals 8
    .parameter "key"
    .parameter "data"
    .parameter "position"

    .prologue
    const/4 v3, 0x1

    const/4 v2, 0x0

    .line 399
    move-object v1, p1

    check-cast v1, Ljava/lang/String;

    .line 400
    .local v1, localPath:Ljava/lang/String;
    new-instance v0, Ljava/io/File;

    invoke-direct {v0, v1}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    .line 401
    .local v0, file:Ljava/io/File;
    invoke-virtual {v0}, Ljava/io/File;->exists()Z

    move-result v4

    if-nez v4, :cond_1

    .line 402
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getThumbnails()Ljava/util/List;

    move-result-object v4

    invoke-interface {v4}, Ljava/util/List;->size()I

    move-result v4

    if-lez v4, :cond_0

    .line 403
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getThumbnails()Ljava/util/List;

    move-result-object v2

    invoke-interface {v2, p3}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Lmiui/resourcebrowser/model/PathEntry;

    invoke-virtual {p0, v2}, Lmiui/resourcebrowser/activity/ResourceAdapter;->downloadThumbnail(Lmiui/resourcebrowser/model/PathEntry;)V

    move v2, v3

    .line 415
    :cond_0
    :goto_0
    return v2

    .line 407
    :cond_1
    invoke-virtual {v0}, Ljava/io/File;->lastModified()J

    move-result-wide v4

    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getUpdatedTime()J

    move-result-wide v6

    cmp-long v4, v4, v6

    if-gez v4, :cond_2

    .line 408
    invoke-virtual {v0}, Ljava/io/File;->delete()Z

    .line 409
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getThumbnails()Ljava/util/List;

    move-result-object v4

    invoke-interface {v4}, Ljava/util/List;->size()I

    move-result v4

    if-lez v4, :cond_0

    .line 410
    invoke-virtual {p2}, Lmiui/resourcebrowser/model/Resource;->getThumbnails()Ljava/util/List;

    move-result-object v2

    invoke-interface {v2, p3}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v2

    check-cast v2, Lmiui/resourcebrowser/model/PathEntry;

    invoke-virtual {p0, v2}, Lmiui/resourcebrowser/activity/ResourceAdapter;->downloadThumbnail(Lmiui/resourcebrowser/model/PathEntry;)V

    move v2, v3

    .line 411
    goto :goto_0

    .line 415
    :cond_2
    invoke-super {p0, p1, p2, p3}, Lmiui/resourcebrowser/widget/AsyncImageAdapter;->isValidKey(Ljava/lang/Object;Ljava/lang/Object;I)Z

    move-result v2

    goto :goto_0
.end method

.method public setResourceBatchHandler(Lmiui/resourcebrowser/util/BatchResourceHandler;)V
    .locals 0
    .parameter "handler"

    .prologue
    .line 102
    iput-object p1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mBatchHandler:Lmiui/resourcebrowser/util/BatchResourceHandler;

    .line 103
    return-void
.end method

.method public setResourceController(Lmiui/resourcebrowser/controller/ResourceController;)V
    .locals 0
    .parameter "resController"

    .prologue
    .line 127
    iput-object p1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mResController:Lmiui/resourcebrowser/controller/ResourceController;

    .line 128
    return-void
.end method

.method protected updateNoResultText()V
    .locals 3

    .prologue
    .line 582
    iget-object v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mFragment:Lmiui/resourcebrowser/activity/BaseFragment;

    if-eqz v1, :cond_0

    iget-object v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mFragment:Lmiui/resourcebrowser/activity/BaseFragment;

    invoke-virtual {v1}, Lmiui/resourcebrowser/activity/BaseFragment;->isVisible()Z

    move-result v1

    if-eqz v1, :cond_0

    .line 583
    iget-object v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mFragment:Lmiui/resourcebrowser/activity/BaseFragment;

    invoke-virtual {v1}, Lmiui/resourcebrowser/activity/BaseFragment;->getView()Landroid/view/View;

    move-result-object v1

    const v2, 0x60b0050

    invoke-virtual {v1, v2}, Landroid/view/View;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    move-object v0, v1

    check-cast v0, Landroid/widget/TextView;

    .line 584
    .local v0, seeMoreTextView:Landroid/widget/TextView;
    invoke-virtual {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->getCount()I

    move-result v1

    if-nez v1, :cond_1

    .line 585
    const v1, 0x60c024e

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(I)V

    .line 586
    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    .line 591
    .end local v0           #seeMoreTextView:Landroid/widget/TextView;
    :cond_0
    :goto_0
    return-void

    .line 588
    .restart local v0       #seeMoreTextView:Landroid/widget/TextView;
    :cond_1
    const/16 v1, 0x8

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setVisibility(I)V

    goto :goto_0
.end method

.method protected useLowQualityDecoding()Z
    .locals 1

    .prologue
    .line 471
    iget-boolean v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter;->mDecodeImageLowQuality:Z

    return v0
.end method


//SMALI - ResourceAdapter$1.smali

.class synthetic Lmiui/resourcebrowser/activity/ResourceAdapter$1;
.super Ljava/lang/Object;
.source "ResourceAdapter.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lmiui/resourcebrowser/activity/ResourceAdapter;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x1008
    name = null
.end annotation

//SMALI - ResourceAdapter$LRUDownload.smali

.class Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;
.super Ljava/lang/Object;
.source "ResourceAdapter.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lmiui/resourcebrowser/activity/ResourceAdapter;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "LRUDownload"
.end annotation


# instance fields
.field private final MAX_NUMBER_OF_DOWNLOAD_TASK:I

.field private mCurrentParallelTaskNumber:I

.field private mDownloadingJobsSet:Ljava/util/Set;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Set",
            "<",
            "Lmiui/resourcebrowser/model/PathEntry;",
            ">;"
        }
    .end annotation
.end field

.field private mFinishJobsQueue:Ljava/util/Map;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Map",
            "<",
            "Lmiui/resourcebrowser/model/PathEntry;",
            "Ljava/lang/Long;",
            ">;"
        }
    .end annotation
.end field

.field private mWaitingJobsQueue:Ljava/util/Map;
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "Ljava/util/Map",
            "<",
            "Lmiui/resourcebrowser/model/PathEntry;",
            "Ljava/lang/Void;",
            ">;"
        }
    .end annotation
.end field

.field final synthetic this$0:Lmiui/resourcebrowser/activity/ResourceAdapter;


# direct methods
.method private constructor <init>(Lmiui/resourcebrowser/activity/ResourceAdapter;)V
    .locals 4
    .parameter

    .prologue
    const/4 v3, 0x0

    .line 489
    iput-object p1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->this$0:Lmiui/resourcebrowser/activity/ResourceAdapter;

    invoke-direct/range {p0 .. p0}, Ljava/lang/Object;-><init>()V

    .line 491
    sget-boolean v0, Lmiui/resourcebrowser/util/ResourceDebug;->DEBUG:Z

    if-eqz v0, :cond_0

    invoke-static {}, Lmiui/resourcebrowser/util/ResourceDebug;->getMaxThumbnailDownloadTaskNumber()I

    move-result v0

    :goto_0
    iput v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->MAX_NUMBER_OF_DOWNLOAD_TASK:I

    .line 494
    new-instance v0, Ljava/util/LinkedHashMap;

    const/high16 v1, 0x3f40

    const/4 v2, 0x1

    invoke-direct {v0, v3, v1, v2}, Ljava/util/LinkedHashMap;-><init>(IFZ)V

    iput-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mWaitingJobsQueue:Ljava/util/Map;

    .line 495
    new-instance v0, Ljava/util/LinkedHashSet;

    invoke-direct {v0}, Ljava/util/LinkedHashSet;-><init>()V

    iput-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mDownloadingJobsSet:Ljava/util/Set;

    .line 496
    new-instance v0, Ljava/util/HashMap;

    invoke-direct {v0}, Ljava/util/HashMap;-><init>()V

    iput-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mFinishJobsQueue:Ljava/util/Map;

    .line 498
    iput v3, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mCurrentParallelTaskNumber:I

    return-void

    .line 491
    :cond_0
    const/4 v0, 0x3

    goto :goto_0
.end method

.method synthetic constructor <init>(Lmiui/resourcebrowser/activity/ResourceAdapter;Lmiui/resourcebrowser/activity/ResourceAdapter$1;)V
    .locals 0
    .parameter "x0"
    .parameter "x1"

    .prologue
    .line 489
    invoke-direct {p0, p1}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;-><init>(Lmiui/resourcebrowser/activity/ResourceAdapter;)V

    return-void
.end method

.method static synthetic access$100(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)Ljava/util/Set;
    .locals 1
    .parameter "x0"

    .prologue
    .line 489
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mDownloadingJobsSet:Ljava/util/Set;

    return-object v0
.end method

.method static synthetic access$200(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)Ljava/util/Map;
    .locals 1
    .parameter "x0"

    .prologue
    .line 489
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mFinishJobsQueue:Ljava/util/Map;

    return-object v0
.end method

.method static synthetic access$300(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)I
    .locals 1
    .parameter "x0"

    .prologue
    .line 489
    iget v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mCurrentParallelTaskNumber:I

    return v0
.end method

.method static synthetic access$306(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)I
    .locals 1
    .parameter "x0"

    .prologue
    .line 489
    iget v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mCurrentParallelTaskNumber:I

    add-int/lit8 v0, v0, -0x1

    iput v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mCurrentParallelTaskNumber:I

    return v0
.end method

.method static synthetic access$400(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)Ljava/util/Map;
    .locals 1
    .parameter "x0"

    .prologue
    .line 489
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mWaitingJobsQueue:Ljava/util/Map;

    return-object v0
.end method

.method static synthetic access$500(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)V
    .locals 0
    .parameter "x0"

    .prologue
    .line 489
    invoke-direct {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->startOneDownloadTask()V

    return-void
.end method

.method private getNextDownloadEntry()Lmiui/resourcebrowser/model/PathEntry;
    .locals 3

    .prologue
    .line 512
    const/4 v0, 0x0

    .line 513
    .local v0, entry:Lmiui/resourcebrowser/model/PathEntry;
    iget-object v2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mWaitingJobsQueue:Ljava/util/Map;

    invoke-interface {v2}, Ljava/util/Map;->keySet()Ljava/util/Set;

    move-result-object v2

    invoke-interface {v2}, Ljava/util/Set;->iterator()Ljava/util/Iterator;

    move-result-object v1

    .line 514
    .local v1, iterator:Ljava/util/Iterator;,"Ljava/util/Iterator<Lmiui/resourcebrowser/model/PathEntry;>;"
    :goto_0
    invoke-interface {v1}, Ljava/util/Iterator;->hasNext()Z

    move-result v2

    if-eqz v2, :cond_0

    .line 515
    invoke-interface {v1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v0

    .end local v0           #entry:Lmiui/resourcebrowser/model/PathEntry;
    check-cast v0, Lmiui/resourcebrowser/model/PathEntry;

    .restart local v0       #entry:Lmiui/resourcebrowser/model/PathEntry;
    goto :goto_0

    .line 517
    :cond_0
    invoke-interface {v1}, Ljava/util/Iterator;->remove()V

    .line 518
    return-object v0
.end method

.method private startOneDownloadTask()V
    .locals 6

    .prologue
    .line 522
    iget-object v2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mWaitingJobsQueue:Ljava/util/Map;

    invoke-interface {v2}, Ljava/util/Map;->isEmpty()Z

    move-result v2

    if-nez v2, :cond_0

    iget v2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mCurrentParallelTaskNumber:I

    iget v3, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->MAX_NUMBER_OF_DOWNLOAD_TASK:I

    if-ge v2, v3, :cond_0

    .line 524
    invoke-direct {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->getNextDownloadEntry()Lmiui/resourcebrowser/model/PathEntry;

    move-result-object v0

    .line 525
    .local v0, downloadEntry:Lmiui/resourcebrowser/model/PathEntry;
    if-nez v0, :cond_1

    .line 578
    .end local v0           #downloadEntry:Lmiui/resourcebrowser/model/PathEntry;
    :cond_0
    :goto_0
    return-void

    .line 528
    .restart local v0       #downloadEntry:Lmiui/resourcebrowser/model/PathEntry;
    :cond_1
    iget-object v2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mFinishJobsQueue:Ljava/util/Map;

    invoke-interface {v2, v0}, Ljava/util/Map;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v1

    check-cast v1, Ljava/lang/Long;

    .line 529
    .local v1, lastDownloadFinishTime:Ljava/lang/Long;
    if-eqz v1, :cond_2

    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    move-result-wide v2

    invoke-virtual {v1}, Ljava/lang/Long;->longValue()J

    move-result-wide v4

    sub-long/2addr v2, v4

    const-wide/16 v4, 0x7530

    cmp-long v2, v2, v4

    if-gez v2, :cond_2

    .line 531
    sget-boolean v2, Lmiui/resourcebrowser/util/ResourceDebug;->DEBUG:Z

    if-eqz v2, :cond_0

    .line 532
    const-string v2, "Theme"

    const-string v3, "Interval of thumbnail downloading is too short: cancelled"

    invoke-static {v2, v3}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0

    .line 537
    :cond_2
    iget-object v2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mDownloadingJobsSet:Ljava/util/Set;

    invoke-interface {v2, v0}, Ljava/util/Set;->add(Ljava/lang/Object;)Z

    .line 538
    iget v2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mCurrentParallelTaskNumber:I

    add-int/lit8 v2, v2, 0x1

    iput v2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mCurrentParallelTaskNumber:I

    .line 540
    new-instance v2, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;

    invoke-direct {v2, p0, v0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;-><init>(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;Lmiui/resourcebrowser/model/PathEntry;)V

    const/4 v3, 0x0

    new-array v3, v3, [Ljava/lang/Void;

    invoke-virtual {v2, v3}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->execute([Ljava/lang/Object;)Landroid/os/AsyncTask;

    .line 572
    sget-boolean v2, Lmiui/resourcebrowser/util/ResourceDebug;->DEBUG:Z

    if-eqz v2, :cond_0

    .line 573
    const-string v2, "Theme"

    new-instance v3, Ljava/lang/StringBuilder;

    invoke-direct {v3}, Ljava/lang/StringBuilder;-><init>()V

    const-string v4, "Start one thumbnail downloading task: RemainTaskNumber="

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    iget-object v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mWaitingJobsQueue:Ljava/util/Map;

    invoke-interface {v4}, Ljava/util/Map;->size()I

    move-result v4

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v3

    const-string v4, " ExecutingThreadNumber="

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v3

    iget v4, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mCurrentParallelTaskNumber:I

    invoke-virtual {v3, v4}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v3

    invoke-static {v2, v3}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    goto :goto_0
.end method


# virtual methods
.method public clean()V
    .locals 1

    .prologue
    .line 508
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mWaitingJobsQueue:Ljava/util/Map;

    invoke-interface {v0}, Ljava/util/Map;->clear()V

    .line 509
    return-void
.end method

.method public submitDownloadJob(Lmiui/resourcebrowser/model/PathEntry;)V
    .locals 2
    .parameter "downloadEntry"

    .prologue
    .line 501
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mDownloadingJobsSet:Ljava/util/Set;

    invoke-interface {v0, p1}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z

    move-result v0

    if-nez v0, :cond_0

    .line 502
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mWaitingJobsQueue:Ljava/util/Map;

    const/4 v1, 0x0

    invoke-interface {v0, p1, v1}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 503
    invoke-direct {p0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->startOneDownloadTask()V

    .line 505
    :cond_0
    return-void
.end method
 
//SMALI - ResourceAdapter$LRUDownload$1.smali

.class Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;
.super Landroid/os/AsyncTask;
.source "ResourceAdapter.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->startOneDownloadTask()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation

.annotation system Ldalvik/annotation/Signature;
    value = {
        "Landroid/os/AsyncTask",
        "<",
        "Ljava/lang/Void;",
        "Ljava/lang/Void;",
        "Ljava/lang/Void;",
        ">;"
    }
.end annotation


# instance fields
.field final synthetic this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

.field final synthetic val$downloadEntry:Lmiui/resourcebrowser/model/PathEntry;


# direct methods
.method constructor <init>(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;Lmiui/resourcebrowser/model/PathEntry;)V
    .locals 0
    .parameter
    .parameter

    .prologue
    .line 540
    iput-object p1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    iput-object p2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->val$downloadEntry:Lmiui/resourcebrowser/model/PathEntry;

    invoke-direct {p0}, Landroid/os/AsyncTask;-><init>()V

    return-void
.end method


# virtual methods
.method protected bridge synthetic doInBackground([Ljava/lang/Object;)Ljava/lang/Object;
    .locals 1
    .parameter "x0"

    .prologue
    .line 540
    check-cast p1, [Ljava/lang/Void;

    .end local p1
    invoke-virtual {p0, p1}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->doInBackground([Ljava/lang/Void;)Ljava/lang/Void;

    move-result-object v0

    return-object v0
.end method

.method protected varargs doInBackground([Ljava/lang/Void;)Ljava/lang/Void;
    .locals 4
    .parameter "params"

    .prologue
    .line 543
    new-instance v0, Ljava/io/File;

    iget-object v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->val$downloadEntry:Lmiui/resourcebrowser/model/PathEntry;

    invoke-virtual {v1}, Lmiui/resourcebrowser/model/PathEntry;->getLocalPath()Ljava/lang/String;

    move-result-object v1

    invoke-direct {v0, v1}, Ljava/io/File;-><init>(Ljava/lang/String;)V

    invoke-virtual {v0}, Ljava/io/File;->exists()Z

    move-result v0

    if-nez v0, :cond_0

    .line 544
    new-instance v0, Lmiui/resourcebrowser/controller/online/DownloadFileTask;

    iget-object v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->val$downloadEntry:Lmiui/resourcebrowser/model/PathEntry;

    invoke-virtual {v1}, Lmiui/resourcebrowser/model/PathEntry;->getOnlinePath()Ljava/lang/String;

    move-result-object v1

    invoke-direct {v0, v1}, Lmiui/resourcebrowser/controller/online/DownloadFileTask;-><init>(Ljava/lang/String;)V

    const/4 v1, 0x1

    new-array v1, v1, [Lmiui/resourcebrowser/model/PathEntry;

    const/4 v2, 0x0

    iget-object v3, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->val$downloadEntry:Lmiui/resourcebrowser/model/PathEntry;

    aput-object v3, v1, v2

    invoke-virtual {v0, v1}, Lmiui/resourcebrowser/controller/online/DownloadFileTask;->downloadFiles([Lmiui/resourcebrowser/model/PathEntry;)Z

    .line 546
    :cond_0
    const/4 v0, 0x0

    return-object v0
.end method

.method protected onCancelled()V
    .locals 2

    .prologue
    .line 550
    new-instance v0, Ljava/lang/RuntimeException;

    const-string v1, "Thumbnail downloading task can not be cancelled!"

    invoke-direct {v0, v1}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;)V

    throw v0
.end method

.method protected bridge synthetic onPostExecute(Ljava/lang/Object;)V
    .locals 0
    .parameter "x0"

    .prologue
    .line 540
    check-cast p1, Ljava/lang/Void;

    .end local p1
    invoke-virtual {p0, p1}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->onPostExecute(Ljava/lang/Void;)V

    return-void
.end method

.method protected onPostExecute(Ljava/lang/Void;)V
    .locals 4
    .parameter "result"

    .prologue
    .line 555
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    iget-object v0, v0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->this$0:Lmiui/resourcebrowser/activity/ResourceAdapter;

    invoke-virtual {v0}, Lmiui/resourcebrowser/activity/ResourceAdapter;->notifyDataSetChanged()V

    .line 556
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    #getter for: Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mDownloadingJobsSet:Ljava/util/Set;
    invoke-static {v0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->access$100(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)Ljava/util/Set;

    move-result-object v0

    iget-object v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->val$downloadEntry:Lmiui/resourcebrowser/model/PathEntry;

    invoke-interface {v0, v1}, Ljava/util/Set;->remove(Ljava/lang/Object;)Z

    .line 557
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    #getter for: Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mFinishJobsQueue:Ljava/util/Map;
    invoke-static {v0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->access$200(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)Ljava/util/Map;

    move-result-object v0

    invoke-interface {v0}, Ljava/util/Map;->size()I

    move-result v0

    const/16 v1, 0x32

    if-le v0, v1, :cond_0

    .line 558
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    #getter for: Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mFinishJobsQueue:Ljava/util/Map;
    invoke-static {v0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->access$200(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)Ljava/util/Map;

    move-result-object v0

    invoke-interface {v0}, Ljava/util/Map;->clear()V

    .line 560
    :cond_0
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    #getter for: Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mFinishJobsQueue:Ljava/util/Map;
    invoke-static {v0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->access$200(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)Ljava/util/Map;

    move-result-object v0

    iget-object v1, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->val$downloadEntry:Lmiui/resourcebrowser/model/PathEntry;

    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J

    move-result-wide v2

    invoke-static {v2, v3}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    move-result-object v2

    invoke-interface {v0, v1, v2}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 561
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    invoke-static {v0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->access$306(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)I

    .line 563
    sget-boolean v0, Lmiui/resourcebrowser/util/ResourceDebug;->DEBUG:Z

    if-eqz v0, :cond_1

    .line 564
    const-string v0, "Theme"

    new-instance v1, Ljava/lang/StringBuilder;

    invoke-direct {v1}, Ljava/lang/StringBuilder;-><init>()V

    const-string v2, "Finish one thumbnail downloading task: RemainTaskNumber="

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    iget-object v2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    #getter for: Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mWaitingJobsQueue:Ljava/util/Map;
    invoke-static {v2}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->access$400(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)Ljava/util/Map;

    move-result-object v2

    invoke-interface {v2}, Ljava/util/Map;->size()I

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    const-string v2, " ExecutingThreadNumber="

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v1

    iget-object v2, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    #getter for: Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->mCurrentParallelTaskNumber:I
    invoke-static {v2}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->access$300(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)I

    move-result v2

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-static {v0, v1}, Landroid/util/Log;->d(Ljava/lang/String;Ljava/lang/String;)I

    .line 568
    :cond_1
    iget-object v0, p0, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload$1;->this$1:Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;

    #calls: Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->startOneDownloadTask()V
    invoke-static {v0}, Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;->access$500(Lmiui/resourcebrowser/activity/ResourceAdapter$LRUDownload;)V

    .line 569
    return-void
.end method

//End SMALI Code

