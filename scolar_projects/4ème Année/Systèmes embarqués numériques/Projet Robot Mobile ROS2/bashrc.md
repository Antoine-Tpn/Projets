#printenv | grep -i ROS
ROS_PYTHON_VERSION=3
AMENT_PREFIX_PATH=/opt/ros/humble
PYTHONPATH=/opt/ros/humble/lib/python3.10/site-packages:/opt/ros/humble/local/l$
LD_LIBRARY_PATH=/opt/ros/humble/opt/rviz_ogre_vendor/lib:/opt/ros/humble/lib/x8$
ROS_LOCALHOST_ONLY=0
ROS_DISTRO=humble
export ROS_DOMAIN_ID=5
conda activate E021_3_6
source /opt/ros/humble/setup.bash

# >>> conda initialize >>>

# !! Contents within this block are managed by 'conda init' !!

\__conda_setup="$('/home/TP/tools/miniconda3/bin/conda' 'shell.bash' 'hook' 2> /$
if [ $? -eq 0 ]; then
    eval "$\__conda_setup"
else
if [ -f "/home/TP/tools/miniconda3/etc/profile.d/conda.sh" ]; then
. "/home/TP/tools/miniconda3/etc/profile.d/conda.sh"
else
export PATH="/home/TP/tools/miniconda3/bin:$PATH"
fi
fi
unset \__conda_setup

# 