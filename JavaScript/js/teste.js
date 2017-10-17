    function traceMap() {
        updateWorkspaceBounds();
        if (!GexfJS.graph) {
            return;
        }
        var _identical = GexfJS.areParamsIdentical;
        GexfJS.params.mousePosition = (GexfJS.params.useLens ? (GexfJS.mousePosition ? (GexfJS.mousePosition.x + "," + GexfJS.mousePosition.y) : "out") : null);
        for (var i in GexfJS.params) {
            _identical = _identical && (GexfJS.params[i] == GexfJS.oldParams[i]);
        }
        if (_identical) {
            return;
        }
        for (var i in GexfJS.params) {
            GexfJS.oldParams[i] = GexfJS.params[i];
        }

        GexfJS.globalScale = Math.pow(Math.SQRT2, GexfJS.params.zoomLevel);
        GexfJS.decalageX = (GexfJS.graphZone.width / 2) - (GexfJS.params.centreX * GexfJS.globalScale);
        GexfJS.decalageY = (GexfJS.graphZone.height / 2) - (GexfJS.params.centreY * GexfJS.globalScale);

        var _sizeFactor = GexfJS.globalScale * Math.pow(GexfJS.globalScale, -.15),
            _edgeSizeFactor = _sizeFactor * GexfJS.params.edgeWidthFactor,
            _nodeSizeFactor = _sizeFactor * GexfJS.params.nodeSizeFactor,
            _textSizeFactor = 1;

        GexfJS.ctxGraphe.clearRect(0, 0, GexfJS.graphZone.width, GexfJS.graphZone.height);

        if (GexfJS.params.useLens && GexfJS.mousePosition) {
            GexfJS.ctxGraphe.fillStyle = "rgba(220,220,250,0.4)";
            GexfJS.ctxGraphe.beginPath();
            GexfJS.ctxGraphe.arc(GexfJS.mousePosition.x, GexfJS.mousePosition.y, GexfJS.lensRadius, 0, Math.PI * 2, true);
            GexfJS.ctxGraphe.closePath();
            GexfJS.ctxGraphe.fill();
        }

        var _centralNode = ((GexfJS.params.activeNode != -1) ? GexfJS.params.activeNode : GexfJS.params.currentNode);

        for (var i in GexfJS.graph.nodeList) {
            var _d = GexfJS.graph.nodeList[i];
            _d.actual_coords = {
                x: GexfJS.globalScale * _d.x + GexfJS.decalageX,
                y: GexfJS.globalScale * _d.y + GexfJS.decalageY,
                r: _nodeSizeFactor * _d.r
            };
            _d.withinFrame = ((_d.actual_coords.x + _d.actual_coords.r > 0) && (_d.actual_coords.x - _d.actual_coords.r < GexfJS.graphZone.width) && (_d.actual_coords.y + _d.actual_coords.r > 0) && (_d.actual_coords.y - _d.actual_coords.r < GexfJS.graphZone.height));
            _d.visible = (GexfJS.params.currentNode == -1 || i == _centralNode || GexfJS.params.showEdges);
        }

        var _tagsMisEnValeur = [];

        if (_centralNode != -1) {
            _tagsMisEnValeur = [_centralNode];
        }

        if (!GexfJS.params.isMoving && (GexfJS.params.showEdges || _centralNode != -1)) {

            var _showAllEdges = (GexfJS.params.showEdges && GexfJS.params.currentNode == -1);

            for (var i in GexfJS.graph.edgeList) {
                var _d = GexfJS.graph.edgeList[i],
                    _six = _d.s,
                    _tix = _d.t,
                    _ds = GexfJS.graph.nodeList[_six],
                    _dt = GexfJS.graph.nodeList[_tix];
                var _isLinked = false;
                if (_centralNode != -1) {
                    if (_six == _centralNode) {
                        _tagsMisEnValeur.push(_tix);
                        _coulTag = _dt.B;
                        _isLinked = true;
                        _dt.visible = true;
                    }
                    if (_tix == _centralNode) {
                        _tagsMisEnValeur.push(_six);
                        _coulTag = _ds.B;
                        _isLinked = true;
                        _ds.visible = true;
                    }
                }

                if ((_isLinked || _showAllEdges) && (_ds.withinFrame || _dt.withinFrame) && _ds.visible && _dt.visible) {
                    GexfJS.ctxGraphe.lineWidth = _edgeSizeFactor * _d.W;
                    var _coords = ((GexfJS.params.useLens && GexfJS.mousePosition) ? calcCoord(GexfJS.mousePosition.x, GexfJS.mousePosition.y, _ds.actual_coords) : _ds.actual_coords);
                    _coordt = ((GexfJS.params.useLens && GexfJS.mousePosition) ? calcCoord(GexfJS.mousePosition.x, GexfJS.mousePosition.y, _dt.actual_coords) : _dt.actual_coords);
                    GexfJS.ctxGraphe.strokeStyle = (_isLinked ? _d.C : "rgba(100,100,100,0.2)");
                    traceArc(GexfJS.ctxGraphe, _coords, _coordt, _sizeFactor * 3.5, GexfJS.params.showEdgeArrow && _d.d);
                }
            }

        }

        GexfJS.ctxGraphe.lineWidth = 4;
        GexfJS.ctxGraphe.strokeStyle = "rgba(0,100,0,0.8)";

        if (_centralNode != -1) {
            var _dnc = GexfJS.graph.nodeList[_centralNode];
            _dnc.real_coords = ((GexfJS.params.useLens && GexfJS.mousePosition) ? calcCoord(GexfJS.mousePosition.x, GexfJS.mousePosition.y, _dnc.actual_coords) : _dnc.actual_coords);
        }

        for (var i in GexfJS.graph.nodeList) {
            var _d = GexfJS.graph.nodeList[i];
            if (_d.visible && _d.withinFrame) {
                if (i != _centralNode) {
                    _d.real_coords = ((GexfJS.params.useLens && GexfJS.mousePosition) ? calcCoord(GexfJS.mousePosition.x, GexfJS.mousePosition.y, _d.actual_coords) : _d.actual_coords);
                    _d.isTag = (_tagsMisEnValeur.indexOf(parseInt(i)) != -1);
                    GexfJS.ctxGraphe.beginPath();
                    GexfJS.ctxGraphe.fillStyle = ((_tagsMisEnValeur.length && !_d.isTag) ? _d.G : _d.B);
                    GexfJS.ctxGraphe.arc(_d.real_coords.x, _d.real_coords.y, _d.real_coords.r, 0, Math.PI * 2, true);
                    GexfJS.ctxGraphe.closePath();
                    GexfJS.ctxGraphe.fill();
                }
            }
        }

        for (var i in GexfJS.graph.nodeList) {
            var _d = GexfJS.graph.nodeList[i];
            if (_d.visible && _d.withinFrame) {
                if (i != _centralNode) {
                    var _fs = _d.real_coords.r * _textSizeFactor;
                    if (_d.isTag) {
                        if (_centralNode != -1) {
                            var _dist = Math.sqrt(Math.pow(_d.real_coords.x - _dnc.real_coords.x, 2) + Math.pow(_d.real_coords.y - _dnc.real_coords.y, 2));
                            if (_dist > 80) {
                                _fs = Math.max(GexfJS.params.textDisplayThreshold + 2, _fs);
                            }
                        } else {
                            _fs = Math.max(GexfJS.params.textDisplayThreshold + 2, _fs);
                        }
                    }
                    if (_fs > GexfJS.params.textDisplayThreshold) {
                        GexfJS.ctxGraphe.fillStyle = ((i != GexfJS.params.activeNode) && _tagsMisEnValeur.length && ((!_d.isTag) || (_centralNode != -1)) ? "rgba(60,60,60,0.7)" : "rgb(0,0,0)");
                        GexfJS.ctxGraphe.font = Math.floor(_fs) + "px Arial";
                        GexfJS.ctxGraphe.textAlign = "center";
                        GexfJS.ctxGraphe.textBaseline = "middle";
                        GexfJS.ctxGraphe.fillText(_d.l, _d.real_coords.x, _d.real_coords.y);
                    }
                }
            }
        }

        if (_centralNode != -1) {
            GexfJS.ctxGraphe.fillStyle = _dnc.B;
            GexfJS.ctxGraphe.beginPath();
            GexfJS.ctxGraphe.arc(_dnc.real_coords.x, _dnc.real_coords.y, _dnc.real_coords.r, 0, Math.PI * 2, true);
            GexfJS.ctxGraphe.closePath();
            GexfJS.ctxGraphe.fill();
            GexfJS.ctxGraphe.stroke();
            var _fs = Math.max(GexfJS.params.textDisplayThreshold + 2, _dnc.real_coords.r * _textSizeFactor) + 2;
            GexfJS.ctxGraphe.font = "bold " + Math.floor(_fs) + "px Arial";
            GexfJS.ctxGraphe.textAlign = "center";
            GexfJS.ctxGraphe.textBaseline = "middle";
            GexfJS.ctxGraphe.fillStyle = "rgba(255,255,250,0.8)";
            GexfJS.ctxGraphe.fillText(_dnc.l, _dnc.real_coords.x - 2, _dnc.real_coords.y);
            GexfJS.ctxGraphe.fillText(_dnc.l, _dnc.real_coords.x + 2, _dnc.real_coords.y);
            GexfJS.ctxGraphe.fillText(_dnc.l, _dnc.real_coords.x, _dnc.real_coords.y - 2);
            GexfJS.ctxGraphe.fillText(_dnc.l, _dnc.real_coords.x, _dnc.real_coords.y + 2);
            GexfJS.ctxGraphe.fillStyle = "rgb(0,0,0)";
            GexfJS.ctxGraphe.fillText(_dnc.l, _dnc.real_coords.x, _dnc.real_coords.y);
        }

        GexfJS.ctxMini.putImageData(GexfJS.imageMini, 0, 0);
        var _r = GexfJS.overviewScale / GexfJS.globalScale,
            _x = - _r * GexfJS.decalageX,
            _y = - _r * GexfJS.decalageY,
            _w = _r * GexfJS.graphZone.width,
            _h = _r * GexfJS.graphZone.height;

        GexfJS.ctxMini.strokeStyle = "rgb(220,0,0)";
        GexfJS.ctxMini.lineWidth = 3;
        GexfJS.ctxMini.fillStyle = "rgba(120,120,120,0.2)";
        GexfJS.ctxMini.beginPath();
        GexfJS.ctxMini.fillRect(_x, _y, _w, _h);
        GexfJS.ctxMini.strokeRect(_x, _y, _w, _h);
    }