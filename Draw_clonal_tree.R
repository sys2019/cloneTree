#Usage Rscript Draw_clonal_tree.R Merge.txt T1.txt T2.txt ... outdir
library(clonevol)
args = commandArgs(T)
options(stringsAsFactors = F)
#args[1] = "/disk/fwy/Clone/DB_10/P58/Merge.txt"
#args[2] = "/disk/fwy/Clone/DB_10/P58/T1.txt"
#args[3] = "/disk/fwy/Clone/DB_10/P58/T2.txt"
#args[4] = "/disk/fwy/Clone/DB_10/P58/L1.txt"
#args[5] = "/disk/fwy/Clone/DB_10/P58/L2.txt"
#args[6] = "/disk/fwy/Clone/DB_10/P58/L3.txt"
#args[7] = "/disk/fwy/Clone/DB_10/P58/L4.txt"
#args[8] = "/disk/fwy/Clone/DB_10/P58/L5.txt"
#args[9] = "/disk/fwy/Clone/DB_10/P58/M1.txt"
#args[10] = "/disk/fwy/Clone/DB_10/P58/M2.txt"
#args[11] = "/disk/fwy/Clone/DB_10/P58/M3.txt"
#args[12] = "/disk/fwy/Clone/DB_10/P58/M4.txt"
#args[13] = "/disk/zxl/projects/Nasopharynx_cancer/4.Evolution_Clone_final/3.Output/P58"

Merge = read.csv(args[1], header = T, sep = "\t")

vv = list()
id = character()
for(i in 2:(length(args)-1)){
  sample_id = sub(".txt","",basename(args[i]))
  #  sample_id = sub("P[0-9]+", "", sample_id)
  id = c(id, sample_id)
  vv[[length(vv)+1]] = list(read.csv(args[i], header = T, sep = "\t"))
}
names(vv) = id

outdir = args[length(args)]

index = as.data.frame(matrix(rep(1,length(id)),1))
colnames(index) = names(vv)
merged.trees = list(Merge)
merged.traces = list(NULL)
scores = NULL
probs = NULL
trimmed.merged.trees = list(Merge)
num.matched.models = list(1)

y = list(models=vv, matched=list(index=index,
                                 merged.trees=merged.trees, merged.traces=merged.traces,
                                 scores=scores, probs=probs, trimmed.merged.trees=trimmed.merged.trees),
         num.matched.models=num.matched.models)

source("/disk/zxl/tools/clonevol/R/clonevol.r")
#################################################################################################
plot.tree = function(v, node.shape='circle', display='tree',
                     node.size=50,
                     node.colors=NULL,
                     color.node.by.sample.group=FALSE,
                     color.border.by.sample.group=TRUE,
                     show.legend=TRUE,
                     tree.node.text.size=1,
                     cell.frac.ci=TRUE,
                     node.prefix.to.add=NULL,
                     title='',
                     #show.sample=FALSE,
                     node.annotation='clone',
                     node.label.split.character=NULL,
                     node.num.samples.per.line=NULL,
                     out.prefix=NULL,
                     graphml.out=FALSE,
                     out.format='graphml'){
  library(igraph)
  grps = NULL
  grp.colors = 'black'
  if (color.border.by.sample.group){
    color.node.by.sample.group = FALSE #disable coloring node by group if blanket is used
    #grps = list()
    #for (i in 1:nrow(v)){
    #    grps = c(grps, list(i))
    #}
    grp.colors = v$sample.group.color
    # get stronger color for borders
    #uniq.colors = unique(grp.colors)
    #border.colors = get.clonevol.colors(length(uniq.colors), T)
    #names(border.colors) = uniq.colors
    #grp.colors = border.colors[grp.colors]
    #v$sample.group.border.color = grp.colors
  }else if (color.node.by.sample.group){
    node.colors = v$sample.group.color
    names(node.colors) = v$lab
  }
  #x = make.graph(v, cell.frac.ci=cell.frac.ci, include.sample.in.label=show.sample, node.colors)
  x = make.graph(v, cell.frac.ci=cell.frac.ci, node.annotation=node.annotation, node.colors)
  #print(v)
  g = x$graph
  v = x$v
  root.idx = which(!is.na(v$parent) & v$parent == '-1')
  #cell.frac = gsub('\\.[0]+$|0+$', '', sprintf('%0.2f%%', v$free*2*100))
  
  
  #V(g)$color = v$color
  #display = 'graph'
  if(display == 'tree'){
    layout = layout.reingold.tilford(g, root=root.idx)
  }else{
    layout = NULL
  }
  
  vertex.labels = V(g)$name
  #vlabs <<- vertex.labels
  if (!is.null(node.label.split.character)){
    num.splits = sapply(vertex.labels, function(l)
      nchar(gsub(paste0('[^', node.label.split.character, ']'), '', l)))
    
    # only keep the node.label.split.char in interval of node.num.samples.per.line
    # such that a block of node.num.samples.per.line samples will be grouped and
    # kept in one line
    if (!is.null(node.num.samples.per.line)){
      for (i in 1:length(vertex.labels)){
        vl = unlist(strsplit(vertex.labels[i], node.label.split.character))
        sel = seq(min(node.num.samples.per.line, length(vl)),
                  length(vl),node.num.samples.per.line)
        vl[sel] = paste0(vl[sel], node.label.split.character)
        vl[-sel] = paste0(vl[-sel], ';')
        vertex.labels[i] = paste(vl, collapse='')
      }
      num.splits = length(sel) + 1
    }
    extra.lf = sapply(num.splits, function(n) paste(rep('\n', n), collapse=''))
    vertex.labels = paste0(extra.lf, gsub(node.label.split.character, '\n',
                                          vertex.labels))
  }
  
  plot(g, edge.color='black', layout=layout, main=title,
       edge.arrow.size=0.75, edge.arrow.width=0.75,
       vertex.shape=node.shape, vertex.size=node.size,
       vertex.label.cex=tree.node.text.size,
       #vertex.label.color=sample(c('black', 'blue', 'darkred'), length(vertex.labels), replace=T),
       vertex.label=vertex.labels,
       #mark.groups = grps,
       #mark.col = 'white',
       #mark.border = grp.colors,
       vertex.frame.color=grp.colors)
  #, vertex.color=v$color, #vertex.label=labels)
  if ((color.node.by.sample.group || color.border.by.sample.group) & show.legend &
      'sample.group' %in% colnames(v)){
    vi = unique(v[!v$excluded & !is.na(v$parent),
                  c('sample.group', 'sample.group.color')])
    vi = vi[order(vi$sample.group),]
    if (color.border.by.sample.group){
      legend('topright', legend=c("Unique location", "Multiple location"), pt.cex=3, cex=1.5,     ### zxl edit "sample.group" into "c("Unique location", "Multiple location")"
             pch=1, col=c("black", "red"))                                               ### zxl edit "sample.group.color" into "col=c("black", "red")"
    }else{
      legend('topright', legend=c("Unique location", "Multiple location"), pt.cex=3, cex=1.5,    ### zxl edit "sample.group" into "c("Unique location", "Multiple location")"
             pch=16, col=vi$sample.group.color)
    }
    legend('bottomleft', legend=c('*  sample founding clone',
                                  '\u00B0  zero cellular fraction',
                                  '\u00B0* ancestor of sample founding clone'
    ),
    pch=c('', '', ''))
    # events on each clone legend
    if ('events' %in% colnames(v)){
      v$events = as.character(v$events)                        ### zxl edit
      ve = v[v$events != '',]
      ve = ve[order(as.integer(ve$lab)),]
      # only print 5 events per-line
      ve$events = insert.lf(ve$events, 5, ',')
      legend('topleft', legend=paste0(sprintf('%2s', ve$lab), ': ', ve$events),
             pt.cex=2, cex=1, pch=19, col=ve$color)
    }
  }
  
  # remove newline char because Cytoscape does not support multi-line label
  V(g)$name = gsub('\n', ' ', V(g)$name, fixed=TRUE)
  if (!is.null(node.prefix.to.add)){
    V(g)$name = paste0(node.prefix.to.add, V(g)$name)
  }
  
  if (!is.null(out.prefix)){
    out.file = paste0(out.prefix, '.', out.format)
    #cat('Writing tree to ', out.file, '\n')
    if (graphml.out){
      write.graph(g, file=out.file, format=out.format)
    }
  }
  
  return(g)
  
}



draw.sample.clones = function(v, x=1, y=0, wid=30, len=9,
                              clone.shape='bell',
                              bell.curve.step=0.25,
                              bell.border.width=1,
                              clone.time.step.scale=1,
                              label=NULL, text.size=1,
                              cell.frac.ci=FALSE,
                              disable.cell.frac=FALSE,
                              zero.cell.frac.clone.color=NULL,
                              zero.cell.frac.clone.border.color=NULL,
                              nonzero.cell.frac.clone.border.color=NULL,
                              nonzero.cell.frac.clone.border.width=NULL,
                              zero.cell.frac.clone.border.width=NULL,
                              top.title=NULL,
                              adjust.clone.height=TRUE,
                              cell.frac.top.out.space=0.75,
                              cell.frac.side.arrow.width=1.5,
                              variants.to.highlight=NULL,
                              variant.color='blue',
                              variant.angle=NULL,
                              show.time.axis=TRUE,
                              color.node.by.sample.group=FALSE,
                              color.border.by.sample.group=TRUE,
                              show.clone.label=TRUE,
                              wscale=1){
  v = v[!v$excluded,]
  if (adjust.clone.height){
    #cat('Will call rescale.vaf on', label, '\n')
    #print(v)
    v = rescale.vaf(v)
    
  }
  # scale VAF so that set.position works properly, and drawing works properly
  max.vaf = max(v$vaf)
  scale = 0.5/max.vaf
  v$vaf = v$vaf*scale
  max.vaf = max(v$vaf)
  high.vaf = max.vaf - 0.02
  low.vaf = 0.2
  y.out <<- wid*max.vaf/2+0.5
  x.out.shift <<- 0.1
  
  wscale = wscale*clone.time.step.scale
  
  #print(v)
  
  
  draw.sample.clone <- function(i){
    vi = v[i,]
    #debug
    #cat('drawing', vi$lab, '\n')
    if (vi$vaf > 0){
      #if (vi$parent == 0){# root
      #if (is.na(vi$parent)){
      if (!is.na(vi$parent) && vi$parent == -1){
        xi = x
        yi = y
        leni = len
      }else{
        # for bell curve, needs to shift x further to make sure
        # bell of subclone falls completely in its parent bell
        x.shift = 1 * ifelse(clone.shape=='bell', 1.2, 1)
        
        if (vi$y.shift + vi$vaf >= high.vaf && vi$vaf < low.vaf){
          x.shift = 2*x.shift
        }
        if (clone.shape=='triangle'){
          x.shift = x.shift + 1
        }
        par = v[v$lab == vi$parent,]
        
        if (vi$vaf < 0.05 && par$num.subclones > 1){x.shift = x.shift*2}
        x.shift = x.shift*clone.time.step.scale*len/7*wscale
        xi = par$x + x.shift
        
        yi = par$y - wid*par$vaf/2 + wid*vi$vaf/2 + vi$y.shift*wid
        leni = par$len - x.shift
      }
      #cell.frac.position = ifelse(vi$free.lower < 0.05 & vi$vaf > 0.25, 'side', 'top.right')
      #cell.frac.position = ifelse(vi$free.lower < 0.05, 'top.out', 'top.right')
      cell.frac.position = ifelse(vi$free < 0.05, 'top.out', 'top.right')
      #cell.frac.position = ifelse(vi$free < 0.05, 'top.out', 'right.mid')
      #cell.frac.position = ifelse(vi$free < 0.05, 'top.out', 'top.out')
      #cell.frac.position = ifelse(vi$num.subclones > 0 , 'right.top', 'right.mid')
      #cell.frac.position = 'top.mid'
      cell.frac = paste0(gsub('\\.[0]+$|0+$', '',
                              sprintf('%0.2f', vi$free.mean*2*100)), '%')
      if(cell.frac.ci && !disable.cell.frac){
        cell.frac = get.cell.frac.ci(vi, include.p.value=TRUE)$cell.frac.ci
      }else if (disable.cell.frac){
        cell.frac = NA
      }
      variant.names = variants.to.highlight$variant.name[
        variants.to.highlight$cluster == vi$lab]
      if (length(variant.names) == 0) {
        variant.names = NULL
      }
      clone.color = vi$color
      border.color='black'
      if (color.border.by.sample.group){
        border.color = vi$sample.group.color
      }else if (color.node.by.sample.group){
        clone.color = vi$sample.group.color
      }
      if (!is.null(zero.cell.frac.clone.border.color) & vi$is.zero){
        border.color = zero.cell.frac.clone.border.color
        if (border.color == 'fill'){border.color = clone.color}
      }
      if (!is.null(zero.cell.frac.clone.color) & vi$is.zero){
        clone.color = zero.cell.frac.clone.color
      }
      
      if (!is.null(nonzero.cell.frac.clone.border.color) & !vi$is.zero){
        border.color = nonzero.cell.frac.clone.border.color
        if (border.color == 'fill'){border.color = clone.color}
      }
      if (!is.null(nonzero.cell.frac.clone.border.width) & !vi$is.zero){
        bell.border.width = nonzero.cell.frac.clone.border.width
      }
      if (!is.null(zero.cell.frac.clone.border.width) & vi$is.zero){
        bell.border.width = zero.cell.frac.clone.border.width
      }
      
      clone.label = ""
      if (show.clone.label){clone.label = vi$lab}
      draw.clone(xi, yi, wid=wid*vi$vaf, len=leni, col=clone.color,
                 clone.shape=clone.shape,
                 bell.curve.step=bell.curve.step,
                 border.width=bell.border.width,
                 #label=vi$lab,
                 label=clone.label,
                 cell.frac=cell.frac,
                 cell.frac.position=cell.frac.position,
                 cell.frac.side.arrow.col=clone.color,
                 text.size=text.size,
                 cell.frac.top.out.space=cell.frac.top.out.space,
                 cell.frac.side.arrow.width=cell.frac.side.arrow.width,
                 variant.names=NULL,               ####### zxl edit "variant.names" into "NULL"
                 variant.color=variant.color,
                 variant.angle=variant.angle,
                 border.color=border.color,
                 wscale=wscale)
      v[i,]$x <<- xi
      v[i,]$y <<- yi
      v[i,]$len <<- leni
      for (j in 1:nrow(v)){
        #cat('---', v[j,]$parent,'\n')
        if (!is.na(v[j,]$parent) && v[j,]$parent != -1 &&
            v[j,]$parent == vi$lab){
          draw.sample.clone(j)
        }
      }
    }
    # draw time axis
    if (show.time.axis && i==1){
      axis.y = -9
      arrows(x0=x,y0=axis.y,x1=10,y1=axis.y, length=0.05, lwd=0.5)
      text(x=10, y=axis.y-0.75, label='time', cex=1, adj=1)
      segments(x0=x,y0=axis.y-0.2,x1=x, y1=axis.y+0.2)
      text(x=x,y=axis.y-0.75,label='Cancer initiated', cex=1, adj=0)
      segments(x0=x+len,y0=axis.y-0.2,x1=x+len, y1=axis.y+0.2)
      text(x=x+len, y=axis.y-0.75, label='Sample taken', cex=1, adj=1)
    }
  }
  plot(c(0, 10),c(-10,10), type = "n", xlab='', ylab='', xaxt='n',
       yaxt='n', axes=FALSE)
  if (!is.null(label)){
    text(x-1*wscale, y, label=label, srt=90, cex=text.size, adj=c(0.5,1))
    #text(x, y, label=label, srt=90, cex=text.size, adj=c(0.5,1))
  }
  if (!is.null(top.title)){
    text(x, y+10, label=top.title, cex=(text.size), adj=c(0,0.5))
  }
  
  # move root to the first row and plot
  root = v[!is.na(v$parent) & v$parent == -1,]
  v = v[is.na(v$parent) | v$parent != -1,]
  v = rbind(root, v)
  v = set.position(v)
  v$x = 0
  v$y = 0
  v$len = 0
  
  #debug
  #print(v)
  
  draw.sample.clone(1)
}
#################################################################################################

plot.clonal.models(y,
                   # bell plot parameters
                   clone.shape = 'bell',
                   bell.event = TRUE,
                   bell.event.label.color = 'blue',
                   bell.event.label.angle = 60,
                   clone.time.step.scale = 1,
                   bell.curve.step = 2,
                   # node-based consensus tree parameters
                   merged.tree.plot = TRUE,
                   tree.node.label.split.character = NULL,
                   tree.node.shape = 'circle',
                   tree.node.size = 30,
                   tree.node.text.size = 0.5,
                   merged.tree.node.size.scale = 1.25,
                   merged.tree.node.text.size.scale = 2.5,
                   merged.tree.cell.frac.ci = FALSE,
                   # branch-based consensus tree parameters
                   merged.tree.clone.as.branch = TRUE,
                   mtcab.event.sep.char = ',',
                   mtcab.branch.text.size = 1,
                   mtcab.branch.width = 0.5,
                   mtcab.node.size = 7,
                   mtcab.node.label.size = 1,
                   mtcab.node.text.size = 1.5,
                   mtcab.show.event = FALSE,
                   mtcab.branch.angle = 30,
                   # cellular population parameters
                   cell.plot = TRUE,
                   num.cells = 100,
                   cell.border.size = 0.25,
                   cell.border.color = 'black',
                   clone.grouping = 'horizontal',
                   #meta-parameters
                   scale.monoclonal.cell.frac = TRUE,
                   show.score = FALSE,
                   cell.frac.ci = FALSE,
                   disable.cell.frac = FALSE,
                   # output figure parameters
                   out.dir = outdir,
                   out.format = 'pdf',
                   overwrite.output = TRUE,
                   width = 16,
                   height = 8,
                   # vector of width scales for each panel from left to right
                   panel.widths = c(4,2,4,4))

